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
        Log.info("开始执行code mapping过程");
        long startTime = System.currentTimeMillis();

        // 生成code token向量
        generateCodeTokenVector();

        // 执行code mapping计算
        doCalculateCodeMappings();

        long endTime = System.currentTimeMillis();
        Log.info("生成code mapping关系完成，耗时（毫秒）：" + (endTime - startTime));
    }

    public void generateCodeTokenVector() {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        // 根据任务id查所有token序列
        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(taskParameter.getTaskId());

        // 生成api token文本语料库
        generateTokenCorpus(apiBasics);

        // 生成api token向量
        generateTokenVector();
    }

    public void doCalculateCodeMappings() {
        Log.info("开始计算code mapping");
        long startTime = System.currentTimeMillis();

        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        // api有类型字段，这里的mapping计算不包括get/set/toString
        // 调用python相似度计算脚本
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

            // 构建mapping结构
            List<ApiMapping> apiMappingList = buildApiMappingList(apiMappings);

            // 构建存在mapping关系的api查询表
            Map<Integer, ApiBasic> apiBasicMap = buildMappingApiBasicMap(apiMappingList);

            // 过滤掉重复mapping
            apiMappingList = filterSameApiMapping(apiMappingList, apiBasicMap);

            // mapping结果保存到数据库，便于后续使用和分析
            batchSave(apiMappingList);

            // 保存到mapping规则中
            doCalculateMappingRule(apiMappingList, apiBasicMap);
        } catch (Exception e) {
            Log.error("计算mapping关系出错");
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        Log.info("执行code mapping计算完毕，耗时（秒）：" + (endTime - startTime) / 1000);
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

    /**
     * 过滤掉重复的mapping：classname 和 api相同
     */
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

        Log.info("将api mapping写入到配置文件");

        // 将api mapping写入到配置文件中
        writeApiMappingToProperties(apiMappingList, apiBasicMap);

        // 计算class mapping
        Map<String, List<String>> classNameMapping = calculateClassNameMapping(apiMappingList, apiBasicMap);

        // 计算get/set(相当于是类属性mapping) mapping， 保存到api mapping配置文件
        calculatePropertyApiMapping(classNameMapping);
    }

    private void writeApiMappingToProperties(List<ApiMapping> apiMappingList, Map<Integer, ApiBasic> apiBasicMap) {
        if (CollectionUtils.isEmpty(apiMappingList)) {
            return;
        }

        apiMappingList.forEach(mapping -> {
            ApiBasic sourceApi = apiBasicMap.get(mapping.getSourceApiId());
            ApiBasic targetApi = apiBasicMap.get(mapping.getTargetApiId());
            // 安卓的api
            String key = sourceApi.getClassName() + Constants.SEPARATOR.ARROW + sourceApi.getApiName();
            // 鸿蒙的api
            String value = targetApi.getClassName() + Constants.SEPARATOR.ARROW + targetApi.getApiName();

            MappingRuleWriter.writeApiMappingProperties(key, value);
        });
    }

    private Map<String, List<String>> calculateClassNameMapping(List<ApiMapping> apiMappingList, Map<Integer, ApiBasic> apiBasicMap) {
        Log.info("开始计算类mapping");
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
        Log.info("类mapping计算完成，耗时[毫秒]：" + (endTime - startTime));

        return classNameMapping;
    }

    private void calculatePropertyApiMapping(Map<String, List<String>> classNameMapping) {
        Log.info("开始计算属性mapping");
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
        Log.info("属性mapping计算完成, 耗时[毫秒]：" + (endTime - startTime));
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
                    // source的api
                    String key = sourceNonApi.getClassName() + Constants.SEPARATOR.ARROW + sourceNonApi.getApiName();
                    // target的api
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

            // 写入配置文件
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

    /**
     * 根据api序列生成文本语料库，用于生成vec
     */
    private void generateTokenCorpus(List<ApiBasic> apiBasics) {
        Log.info("开始生成code token corpus");
        long startTime = System.currentTimeMillis();

        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        // 取出所有token序列
        List<String> tokens = Lists.newArrayList();
        for (ApiBasic apiBasic : apiBasics) {
            tokens.add(apiBasic.getTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
            tokens.add(apiBasic.getClassNameTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
            tokens.add(apiBasic.getClassCommentTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
            tokens.add(apiBasic.getMethodCommentTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
            tokens.add(apiBasic.getMethodParamReturnTokenSequence().replace(Constants.SEPARATOR.COMMA, Constants.SEPARATOR.SPACE));
        }
        tokens = tokens.stream().distinct().collect(Collectors.toList());

        // 填充token语料库文件
        fillCorpus(tokens);

        // 调用python生成词向量文件
        String[] args = new String[]{
                taskParameter.getPythonBinPath(),
                taskParameter.getPythonWordVec(),
                taskParameter.getCorpusFilepath(),
                taskParameter.getWordVecModelFilepath()
        };

        CallUtil.call(args);

        long endTime = System.currentTimeMillis();
        Log.info("code token corpus生成完成, 耗时[毫秒]：" + (endTime - startTime));
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

    /**
     * 调用python来生成向量字典，并将向量字典存储到对应的文件（xxFilepath）中
     */
    private void generateTokenVector() {
        Log.info("开始生成code token vector");
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
        Log.info("code token vector生成完成, 耗时[毫秒]:" + (endTime - startTime));
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


    public static void main(String[] args) {
        ApiMappingService apiMappingService = new ApiMappingService();
        try {
            String resultLine = "[['3-937', '10-3360', '11-3360', '16-3970', '36-1336', '37-518', '38-518', '39-518', '40-518'," +
                    " '42-518', '43-2594', '44-2594', '45-2594', '46-2594', '47-1255', '48-2594', '49-2594', '50-2594', '64-1652', " +
                    "'69-1652', '82-988', '109-516', '110-516', '128-1465', '129-1465', '130-1465', '138-1637', '169-3345', '170-3347', " +
                    "'171-518', '173-1711', '182-1200', '202-3970', '221-2078', '228-2646', '234-2646', '242-3345', '243-3347', '244-698'," +
                    " '245-2646', '246-2646', '254-2642', '255-3301', '273-3360', '284-2836']]";

            List<String> apiMappings = JsonUtil.jsonToList(resultLine.toString(), String.class);

            // 构建mapping结构
            List<ApiMapping> apiMappingList = apiMappingService.buildApiMappingList(apiMappings);

            // 构建存在mapping关系的api查询表
            Map<Integer, ApiBasic> apiBasicMap = apiMappingService.buildMappingApiBasicMap(apiMappingList);

            // 过滤掉重复mapping
            apiMappingList = apiMappingService.filterSameApiMapping(apiMappingList, apiBasicMap);

            // 保存到mapping规则中
            apiMappingService.doCalculateMappingRule(apiMappingList, apiBasicMap);
        } catch (Exception e) {
            Log.error("计算mapping关系出错");
            e.printStackTrace();
        }
    }


}
