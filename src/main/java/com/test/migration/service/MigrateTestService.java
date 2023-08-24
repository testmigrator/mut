package com.test.migration.service;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.dao.MigrateTestDao;
import com.test.migration.entity.Constants;
import com.test.migration.entity.TaskParameter;
import com.test.migration.entity.TestMethodParseBasic;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import com.test.migration.entity.po.MigrateTest;
import com.test.migration.service.invocation.ApiInvocationVisitor;
import com.test.migration.service.preprocess.CommonBasicService;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.CollectionUtils;
import utils.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MigrateTestService {
    ApiBasicService apiBasicService = new ApiBasicService();
    ApiMappingService apiMappingService = new ApiMappingService();


    public void generateMigrateTest() {
        long startTime = System.currentTimeMillis();

        try {
            Map<String, List<ApiBasic>> mappingSourceApiMap = generateMappingSourceApiMap();

            List<MigrateTest> migrateTestList = generateMigrateTestList(mappingSourceApiMap);

            batchSave(migrateTestList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long endTime = System.currentTimeMillis();
    }

    /**
     * @return key:api
     */
    private Map<String, List<ApiBasic>> generateMappingSourceApiMap() {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(taskParameter.getTaskId());
        List<Integer> sourceApiIds = apiMappings.stream()
                .map(ApiMapping::getSourceApiId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(sourceApiIds)) {
            return Maps.newHashMap();
        }

        List<ApiBasic> apiBasics = apiBasicService.selectByIds(sourceApiIds);

        return apiBasics.stream().collect(Collectors.groupingBy(ApiBasic::getFilepath));
    }

    private List<MigrateTest> generateMigrateTestList(Map<String, List<ApiBasic>> mappingSourceApiMap) {
        List<MigrateTest> migrateTestList = Lists.newArrayList();

        List<String> sourceTestFilepathList = fetchSourceTestFilepathList();

        // log
        Log.info("sourceTestFilepathList: " + Joiner.on(",").join(sourceTestFilepathList));

        for (Map.Entry<String, List<ApiBasic>> entry : mappingSourceApiMap.entrySet()) {
            String apiFilepath = entry.getKey();
            List<ApiBasic> fileApis = entry.getValue();

            List<MigrateTest> migrateTestByClassList = generateMigrateTestByClass(apiFilepath, sourceTestFilepathList, fileApis);
            migrateTestList.addAll(migrateTestByClassList);
        }


        List<MigrateTest> mergedMigrateTests = mergeMigrateTest(migrateTestList);

        // print
        reportMigrateTest(mergedMigrateTests);

        return mergedMigrateTests;
    }

    /**
     * @param migrateTestList
     * @return
     */
    private List<MigrateTest> mergeMigrateTest(List<MigrateTest> migrateTestList) {
        Map<String, List<MigrateTest>> migrateTestGroup = migrateTestList.stream()
                .collect(Collectors.groupingBy(MigrateTest::getTestFilepath));

        List<MigrateTest> mergedMigrateTests = Lists.newArrayList();

        migrateTestGroup.forEach((testFilepath, migrateTests) -> {
            MigrateTest firstMigrateTest = migrateTests.get(0);
            if (migrateTests.size() == 1) {
                mergedMigrateTests.add(firstMigrateTest);
                return;
            }
            List<String> testMethodApiInvocations = migrateTests.stream().map(MigrateTest::getTestMethodApiInvocation)
                    .collect(Collectors.toList());
            String mergedTestMethodApiInvocation = mergeTestMethodApiInvocation(testMethodApiInvocations);

            List<String> referenceTypes = migrateTests.stream().map(MigrateTest::getReferenceType)
                    .collect(Collectors.toList());
            String mergedReferenceType = mergeReferenceType(referenceTypes);

            MigrateTest mergedMigrateTest = MigrateTest.builder()
                    .taskId(firstMigrateTest.getTaskId())
                    .testFilepath(firstMigrateTest.getTestFilepath())
                    .testMethodApiInvocation(mergedTestMethodApiInvocation)
                    .referenceType(mergedReferenceType)
                    .build();
            mergedMigrateTests.add(mergedMigrateTest);
        });

        return mergedMigrateTests;

    }

    private String mergeTestMethodApiInvocation(List<String> testMethodApiInvocations) {
        Map<String, List<Integer>> testMethodApiInvocationMap = Maps.newHashMap();
        testMethodApiInvocations.forEach(testMethodApiInvocation -> {
                    Map<String, List<Integer>> map = JsonUtil.jsonToPojo(testMethodApiInvocation, Map.class);
                    if (map == null) {
                        return;
                    }
                    map.forEach((testMethodName, apiIds) -> {
                        if (testMethodApiInvocationMap.containsKey(testMethodName)) {
                            testMethodApiInvocationMap.get(testMethodName).addAll(apiIds);
                        } else {
                            testMethodApiInvocationMap.put(testMethodName, apiIds);
                        }
                    });
                }
        );

        return JsonUtil.objectToJson(testMethodApiInvocationMap);
    }

    private String mergeReferenceType(List<String> referenceTypes) {
        if (CollectionUtils.isEmpty(referenceTypes)) {
            return StringUtils.EMPTY;
        }
        return referenceTypes.get(0);
    }


    /**
     * @param migrateTestList
     * @return
     */
    private void filterTestMethodWithInvokeRule(List<MigrateTest> migrateTestList) {
        for (MigrateTest migrateTest : migrateTestList) {
            Map<String, List<Integer>> testMethodApiInvocationMap = JsonUtil.jsonToPojo(migrateTest.getTestMethodApiInvocation(), Map.class);
            Map<String, LinkedHashMap> referenceTypeMap = JsonUtil.jsonToPojo(migrateTest.getReferenceType(), Map.class);

            if (testMethodApiInvocationMap == null) {
                Log.error("filterTestMethodWithInvokeRule testMethodApiInvocationMap不应该为null");
                continue;
            }
            if (referenceTypeMap == null) {
                continue;
            }

            Map<String, List<Integer>> filterTestMethodApiInvocationMap = Maps.newHashMap();
            testMethodApiInvocationMap.forEach((methodName, invokeIds) -> {
                LinkedHashMap referenceType = referenceTypeMap.get(methodName);
                if (referenceType == null) {
                    filterTestMethodApiInvocationMap.put(methodName, invokeIds);
                    return;
                }

                boolean isFilterTestByRule = filterTestByRule(invokeIds, referenceType);
                if (!isFilterTestByRule) {
                    filterTestMethodApiInvocationMap.put(methodName, invokeIds);
                }
            });

            migrateTest.setTestMethodApiInvocation(JsonUtil.objectToJson(filterTestMethodApiInvocationMap));
        }
    }

    private boolean filterTestByRule(List<Integer> invokeIds, LinkedHashMap referenceType) {
        boolean isRemove = false;
        int methodTotalLine = (int) referenceType.get("methodTotalLine");
        int invokeApiSize = invokeIds.size();

        if (methodTotalLine > 30) {
            isRemove = true;
        }

        List<String> invokeApis = (List<String>) referenceType.get("invokeApis");

        invokeApis = invokeApis.stream().distinct().collect(Collectors.toList());
        List<String> filterNoNeedApiInvokeApis = Lists.newArrayList();
        for (String apiName : invokeApis) {
            if (apiName.toLowerCase().startsWith("assert")) {
                continue;
            }
            if (apiName.toLowerCase().startsWith("isequal")) {
                continue;
            }
            filterNoNeedApiInvokeApis.add(apiName);
        }
        int totalApiSize = filterNoNeedApiInvokeApis.size();
        if (totalApiSize / invokeApiSize > 5) {
            isRemove = true;
        }

        List<String> useClasses = (List<String>) referenceType.get("useClasses");
        int useClassesSize = useClasses.size();
        if (useClassesSize > 10) {
            isRemove = true;
        }


        if (totalApiSize >= 5 && invokeApiSize <= 3) {
            isRemove = true;
        }


        if (methodTotalLine >= 15 && totalApiSize > 5 && invokeApiSize <= 2) {
            isRemove = true;
        }

        Log.warn("testMethod: " + referenceType.get("methodName") + ": totalLine:" + methodTotalLine
                + ", total invoke api: " + totalApiSize
                + ", invoke mapping api: " + invokeApiSize
                + ", use classes: " + useClassesSize
                + ", is remove?: " + isRemove);

        return isRemove;
    }

    private List<MigrateTest> generateMigrateTestByClass(String apiFilepath, List<String> sourceTestFilepathList, List<ApiBasic> fileApis) {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();
        List<MigrateTest> migrateTestList = Lists.newArrayList();

        String className = CommonBasicService.getClassNameByFilepath(apiFilepath);
        List<String> testFilepathList = CommonBasicService.filterTestFilepathByClassName(sourceTestFilepathList, className);
        for (String testFilepath : testFilepathList) {
            TestMethodParseBasic testMethodParseBasic = parseTestFile(testFilepath);
            if (testMethodParseBasic == null || testMethodParseBasic.getMethodInvocationList() == null) {
                Log.error("buildTestMethodInvokeApiMap error");
                continue;
            }
            Map<String, List<Integer>> testMethodInvocationMap = buildTestMethodInvokeApiMap(testMethodParseBasic, fileApis, className);

            Map<String, TestMethodParseBasic.ReferenceType> testMethodReferenceMap = buildTestMethodReferenceMap(testMethodParseBasic, fileApis, className);

            if (testMethodInvocationMap.isEmpty()) {
                continue;
            }

            migrateTestList.add(MigrateTest.builder()
                    .taskId(taskParameter.getTaskId())
                    .testFilepath(testFilepath)
                    .testMethodApiInvocation(JsonUtil.objectToJson(testMethodInvocationMap))
                    .referenceType(JsonUtil.objectToJson(testMethodReferenceMap))
                    .build());
        }

        return migrateTestList;
    }

    private Map<String, TestMethodParseBasic.ReferenceType> buildTestMethodReferenceMap(TestMethodParseBasic testMethodParseBasic, List<ApiBasic> fileApis, String className) {

        return testMethodParseBasic.getReferenceTypeList().stream()
                .collect(Collectors.toMap(TestMethodParseBasic.ReferenceType::getMethodName,
                        Function.identity(),
                        (x, y) -> x));
    }

    /**
     */
    private Map<String, List<Integer>> buildTestMethodInvokeApiMap(TestMethodParseBasic testMethodParseBasic,
                                                                   List<ApiBasic> fileApis, String className) {
        Map<String, Set<String>> testMethodInvocationMap = testMethodParseBasic.getMethodInvocationList().stream()
                .filter(x -> x.getCallee() != null && x.getCallee().size() > 0)
                .collect(Collectors.toMap(TestMethodParseBasic.MethodInvocation::getCaller,
                        TestMethodParseBasic.MethodInvocation::getCallee, (x, y) -> x));

        // key: testMethodName, value:call api ids
        Map<String, List<Integer>> testMethodInvokeApiMap = Maps.newHashMap();
        for (Map.Entry<String, Set<String>> entry : testMethodInvocationMap.entrySet()) {
            String testMethodName = entry.getKey();
            Set<String> invokeApiNames = entry.getValue();

            List<Integer> apiIds = fileApis.stream()
                    .filter(api -> StringUtils.equals(className, api.getClassName()))
                    .filter(api -> invokeApiNames.contains(api.getApiName()))
                    .map(ApiBasic::getId)
                    .collect(Collectors.toList());

            if (!apiIds.isEmpty()) {
                testMethodInvokeApiMap.put(testMethodName, apiIds);
            }
        }

        return testMethodInvokeApiMap;
    }


    private void reportMigrateTest(List<MigrateTest> migrateTests) {
        int totalInvocationTestNum = 0;
        for (MigrateTest migrateTest : migrateTests) {
            Map<String, List<Integer>> map = JsonUtil.jsonToPojo(migrateTest.getTestMethodApiInvocation(), Map.class);
            if (map == null) {
                continue;
            }
            totalInvocationTestNum += map.keySet().size();
        }
    }

    /**
     */
    private List<String> fetchSourceTestFilepathList() {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<String> targetSourceCodeFilepathList = Splitter.on(Constants.SEPARATOR.COMMA)
                .splitToList(taskParameter.getTargetSourceCodeFilepath());

        return targetSourceCodeFilepathList.stream()
                .flatMap(filepath -> GetFoldFileNames.readfileWithType(filepath, Constants.FILE_TYPE.JAVA).stream()
                        .filter(file -> {
                            if (StringUtils.isNotBlank(taskParameter.getFilepathKey())) {
                                return file.contains(Constants.TEST) && file.toLowerCase().contains(taskParameter.getFilepathKey());
                            }
                            return true;
                        }))
                .distinct()
                .collect(Collectors.toList());
    }

    private TestMethodParseBasic parseTestFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Java8Parser parser = new Java8Parser(new CommonTokenStream(new Java8Lexer(inputStream)));
        ParseTree parseTree = parser.compilationUnit();
        ApiInvocationVisitor methodVisitor = new ApiInvocationVisitor();
        methodVisitor.visit(parseTree);

        return methodVisitor.getTestMethodParseBasic();
    }

    /**
     * CRUD
     **/
    public void batchSave(List<MigrateTest> migrateTests) {
        if (CollectionUtils.isEmpty(migrateTests)) {
            return;
        }

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            MigrateTestDao mapper = session.getMapper(MigrateTestDao.class);
            mapper.batchInsert(migrateTests);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MigrateTest> selectByTaskId(Integer taskId) {
        List<MigrateTest> list = Lists.newArrayList();

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            MigrateTestDao mapper = session.getMapper(MigrateTestDao.class);
            list = mapper.selectByTaskId(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


}
