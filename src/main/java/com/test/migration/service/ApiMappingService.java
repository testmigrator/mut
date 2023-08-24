package com.test.migration.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.test.migration.dao.ApiMappingDao;
import com.test.migration.entity.Constants;
import com.test.migration.entity.TaskParameter;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;
import utils.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApiMappingService {

    ApiBasicService apiBasicService = new ApiBasicService();

    /**
     * 计算code mapping
     */
    public void calculateCodeMappings() {
        long startTime = System.currentTimeMillis();

        generateCodeTokenVector();

        doCalculateCodeMappings();

        long endTime = System.currentTimeMillis();
    }

    public void generateCodeTokenVector() {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(taskParameter.getTaskId());

        generateTokenCorpus(apiBasics);

        generateTokenVector();
    }

    public void doCalculateCodeMappings() {
        long startTime = System.currentTimeMillis();

        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        String[] tokenArgs = new String[]{
                taskParameter.getPythonBinPath(),
                taskParameter.getPythonCalcSimilarity(),
                String.valueOf(taskParameter.getTaskId()),
                taskParameter.getDbFilepath(),
                taskParameter.getApiVectorDictFilepath(),
                taskParameter.getClassVectorDictFilepath(),
                taskParameter.getClassCommentVectorDictFilepath(),
                taskParameter.getMethodCommentVectorDictFilepath(),
                taskParameter.getMethodParamReturnVectorDictFilepath()
        };


        try {
            String resultLine = calculateMappingResult(tokenArgs);

            List<String> apiMappings = JsonUtil.jsonToList(resultLine, String.class);

            List<ApiMapping> apiMappingList = buildApiMappingList(apiMappings);

            Map<Integer, ApiBasic> apiBasicMap = buildMappingApiBasicMap(apiMappingList);

            apiMappingList = filterSameApiMapping(apiMappingList, apiBasicMap);

            batchSave(apiMappingList);

            doCalculateMappingRule(apiMappingList, apiBasicMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
    }

    private String calculateMappingResult(String[] tokenArgs) {
        Log.info("resultLines: start");

        List<String> resultLines = CallUtil.call(tokenArgs);

        resultLines.forEach(System.out::println);
        StringBuilder resultLine = new StringBuilder();
        resultLines.forEach(resultLine::append);

        System.out.println("resultLine:"+resultLine);
        Log.info("resultLines: end");

        return resultLine.toString();
    }

    private List<ApiMapping> filterSameApiMapping(List<ApiMapping> mappings, Map<Integer, ApiBasic> apiBasicMap) {
        if (CollectionUtils.isEmpty(mappings)) {
            return Lists.newArrayList();
        }

        Set<String> mappingKeySet = Sets.newHashSet();
        List<ApiMapping> filterMappings = Lists.newArrayList();

        for (ApiMapping apiMapping : mappings) {
            ApiBasic sourceApi = apiBasicMap.get(apiMapping.getSourceApiId());
            ApiBasic targetApi = apiBasicMap.get(apiMapping.getTargetApiId());

            String mappingKey = sourceApi.getClassName() + "$" + sourceApi.getApiName() + ":"
                    + targetApi.getClassName() + "$" + targetApi.getApiName();

            if (!mappingKeySet.contains(mappingKey)) {
                mappingKeySet.add(mappingKey);
                filterMappings.add(apiMapping);
            }
        }

        return filterMappings;
    }

    private Map<Integer, ApiBasic> buildMappingApiBasicMap(List<ApiMapping> apiMappingList) {
        if (CollectionUtils.isEmpty(apiMappingList)) {
            return Maps.newHashMap();
        }

        List<Integer> apiBasicIds = Lists.newArrayList();
        apiBasicIds.addAll(apiMappingList.stream().map(ApiMapping::getSourceApiId).collect(Collectors.toList()));
        apiBasicIds.addAll(apiMappingList.stream().map(ApiMapping::getTargetApiId).collect(Collectors.toList()));

        List<ApiBasic> apiBasics = apiBasicService.selectByIds(apiBasicIds);

        return apiBasics.stream()
                .collect(Collectors.toMap(ApiBasic::getId, Function.identity()));
    }


    /**
     *
     */
    private void doCalculateMappingRule(List<ApiMapping> apiMappingList, Map<Integer, ApiBasic> apiBasicMap) {
        if (CollectionUtils.isEmpty(apiMappingList)) {
            return;
        }

        writeApiMappingToProperties(apiMappingList, apiBasicMap);

        Map<String, List<String>> classNameMapping = calculateClassNameMapping(apiMappingList, apiBasicMap);

        calculatePropertyApiMapping(classNameMapping);
    }

    private void writeApiMappingToProperties(List<ApiMapping> apiMappingList, Map<Integer, ApiBasic> apiBasicMap) {
        if (CollectionUtils.isEmpty(apiMappingList)) {
            return;
        }

        apiMappingList.forEach(mapping -> {
            ApiBasic sourceApi = apiBasicMap.get(mapping.getSourceApiId());
            ApiBasic targetApi = apiBasicMap.get(mapping.getTargetApiId());
            String key = sourceApi.getClassName() + Constants.SEPARATOR.ARROW + sourceApi.getApiName();
            String value = targetApi.getClassName() + Constants.SEPARATOR.ARROW + targetApi.getApiName();

            MappingRuleWriter.writeApiMappingProperties(key, value);
        });
    }

    private Map<String, List<String>> calculateClassNameMapping(List<ApiMapping> apiMappingList, Map<Integer, ApiBasic> apiBasicMap) {
        long startTime = System.currentTimeMillis();

        Map<String, List<String>> classNameMapping = Maps.newHashMap();

        apiMappingList.forEach(mapping -> {
            ApiBasic sourceApi = apiBasicMap.get(mapping.getSourceApiId());
            ApiBasic targetApi = apiBasicMap.get(mapping.getTargetApiId());

            classNameMapping.computeIfAbsent(sourceApi.getClassName(), k -> Lists.newArrayList())
                    .add(targetApi.getClassName());
        });

        doCalculateClassNameMapping(classNameMapping);

        long endTime = System.currentTimeMillis();

        return classNameMapping;
    }

    private void calculatePropertyApiMapping(Map<String, List<String>> classNameMapping) {
        long startTime = System.currentTimeMillis();

        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<ApiBasic> apis = apiBasicService.selectByTaskId(taskParameter.getTaskId());

        // get/set/tostring
        Map<String, List<ApiBasic>> sourceNonApiMap = apis.stream()
                .filter(x -> x.getType() == Constants.API_TYPE.SOURCE_NON_API)
                .collect(Collectors.groupingBy(ApiBasic::getClassName));

        Map<String, List<ApiBasic>> targetNonApiMap = apis.stream()
                .filter(x -> x.getType() == Constants.API_TYPE.TARGET_NON_API)
                .collect(Collectors.groupingBy(ApiBasic::getClassName));

        // 写入配置文件
        classNameMapping.forEach((sourceClassName, targetClassList) ->
                targetClassList.forEach(targetClassName -> {
                    List<ApiBasic> sourceNonApis = sourceNonApiMap.get(sourceClassName);
                    List<ApiBasic> targetNonApis = targetNonApiMap.get(targetClassName);
                    writePropertyMappingToProperties(sourceNonApis, targetNonApis);
                }));

        long endTime = System.currentTimeMillis();
    }

    private void writePropertyMappingToProperties(List<ApiBasic> sourceNonApis, List<ApiBasic> targetNonApis) {
        if (CollectionUtils.isEmpty(sourceNonApis) || CollectionUtils.isEmpty(targetNonApis)) {
            return;
        }

        for (ApiBasic sourceNonApi : sourceNonApis) {
            for (ApiBasic targetNonApi : targetNonApis) {
                String sourceNonApiName = sourceNonApi.getApiName().toLowerCase();
                String targetNonApiName = targetNonApi.getApiName().toLowerCase();
                if (StringUtils.equals(sourceNonApiName, targetNonApiName)) {
                    String key = sourceNonApi.getClassName() + Constants.SEPARATOR.ARROW + sourceNonApi.getApiName();
                    String value = targetNonApi.getClassName() + Constants.SEPARATOR.ARROW + targetNonApi.getApiName();
                    MappingRuleWriter.writeApiMappingProperties(key, value);
                }
            }
        }
    }

    private void doCalculateClassNameMapping(Map<String, List<String>> classNameMapping) {
        classNameMapping.forEach((sourceClassName, targetClassList) -> {
            String targetClassListStr;
            if (targetClassList.size() == 1) {
                targetClassListStr = targetClassList.get(0);
            } else {
                targetClassList.add(sourceClassName);
                targetClassListStr = Joiner.on(Constants.ClassMappingOneToManySplitter).join(targetClassList);
            }

            try {
                MappingRuleWriter.writeClassNameMappingProperties(sourceClassName, targetClassListStr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<ApiMapping> buildApiMappingList(List<String> apiMappings) {
        if (CollectionUtils.isEmpty(apiMappings)) {
            return Lists.newArrayList();
        }

        Integer taskId = TaskParameterReader.getTaskParameter().getTaskId();

        return apiMappings.stream().map(apiMapping -> {
            String[] split = apiMapping.split(Constants.SEPARATOR.BAR);

            return ApiMapping.builder()
                    .taskId(taskId)
                    .sourceApiId(Integer.parseInt(split[0]))
                    .targetApiId(Integer.parseInt(split[1]))
                    .build();

        }).collect(Collectors.toList());
    }

    private void generateTokenCorpus(List<ApiBasic> apiBasics) {
        long startTime = System.currentTimeMillis();

        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<String> tokens = Lists.newArrayList();
        for (ApiBasic apiBasic : apiBasics) {
            tokens.add(apiBasic.getTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
            tokens.add(apiBasic.getClassNameTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
            tokens.add(apiBasic.getClassCommentTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
            tokens.add(apiBasic.getMethodCommentTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
            tokens.add(apiBasic.getMethodParamReturnTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
        }
        tokens = tokens.stream().distinct().collect(Collectors.toList());

        fillCorpus(tokens);

        String[] args = new String[]{
                taskParameter.getPythonBinPath(),
                taskParameter.getPythonWordVec(),
                taskParameter.getCorpusFilepath(),
                taskParameter.getWordVecModelFilepath()
        };

        CallUtil.call(args);

        long endTime = System.currentTimeMillis();
    }

    private void fillCorpus(List<String> tokens) {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();
        String corpusFilepath = taskParameter.getCorpusFilepath();

        try {
            FileWriteUtil.writeDataToFile(tokens, corpusFilepath);
        } catch (Exception e) {
            Log.error("fillCorpus error");
            e.printStackTrace();
        }
    }

    private void generateTokenVector() {
        long startTime = System.currentTimeMillis();

        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        String[] tokenArgs = new String[]{
                taskParameter.getPythonBinPath(),
                taskParameter.getPythonCalcTokenVec(),
                String.valueOf(taskParameter.getTaskId()),
                taskParameter.getDbFilepath(),
                taskParameter.getWordVecModelFilepath(),
                taskParameter.getApiVectorDictFilepath(),
                taskParameter.getClassVectorDictFilepath(),
                taskParameter.getClassCommentVectorDictFilepath(),
                taskParameter.getMethodCommentVectorDictFilepath(),
                taskParameter.getMethodParamReturnVectorDictFilepath()
        };

        CallUtil.call(tokenArgs);

        long endTime = System.currentTimeMillis();
    }


    /**
     * CRUD
     **/
    public List<ApiMapping> selectByTaskId(Integer taskId) {
        List<ApiMapping> list = Lists.newArrayList();

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiMappingDao mapper = session.getMapper(ApiMappingDao.class);
            list = mapper.selectByTaskId(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void batchSave(List<ApiMapping> apiMappings) {
        if (CollectionUtils.isEmpty(apiMappings)) {
            return;
        }

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiMappingDao mapper = session.getMapper(ApiMappingDao.class);
            mapper.batchInsert(apiMappings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiMappingDao mapper = session.getMapper(ApiMappingDao.class);
            mapper.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}
