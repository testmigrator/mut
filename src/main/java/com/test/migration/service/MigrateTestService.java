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

    /**
     * 存在api映射关系，找目标api合适的测试类，作为待迁移test
     */
    public void generateMigrateTest() {
        long startTime = System.currentTimeMillis();

        try {
            // 获取所有存在mapping关系的source api， key：api所在的文件路径 value：文件下所有api
            Map<String, List<ApiBasic>> mappingSourceApiMap = generateMappingSourceApiMap();

            // 构建待迁移test
            List<MigrateTest> migrateTestList = generateMigrateTestList(mappingSourceApiMap);

            batchSave(migrateTestList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long endTime = System.currentTimeMillis();
        Log.info("筛选待迁移test完成，耗时（毫秒）：" + (endTime - startTime));
    }

    /**
     * 找到所有test api
     *
     * @return key:api所在文件的filepath， value：该文件所有api
     */
    private Map<String, List<ApiBasic>> generateMappingSourceApiMap() {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        // 筛选出存在mapping关系的 source apiIds
        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(taskParameter.getTaskId());
        List<Integer> sourceApiIds = apiMappings.stream()
                .map(ApiMapping::getSourceApiId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(sourceApiIds)) {
            return Maps.newHashMap();
        }

        // 筛选出存在mapping关系的 source api
        List<ApiBasic> apiBasics = apiBasicService.selectByIds(sourceApiIds);

        return apiBasics.stream().collect(Collectors.groupingBy(ApiBasic::getFilepath));
    }

    private List<MigrateTest> generateMigrateTestList(Map<String, List<ApiBasic>> mappingSourceApiMap) {
        List<MigrateTest> migrateTestList = Lists.newArrayList();

        // 获取source对应的所有test code
        List<String> sourceTestFilepathList = fetchSourceTestFilepathList();

        // log
        Log.info("sourceTestFilepathList: " + Joiner.on(",").join(sourceTestFilepathList));

        // 对所有存在mapping关系的source文件依次进行解析
        for (Map.Entry<String, List<ApiBasic>> entry : mappingSourceApiMap.entrySet()) {
            String apiFilepath = entry.getKey();
            List<ApiBasic> fileApis = entry.getValue();

            // 对每个类进行分析
            List<MigrateTest> migrateTestByClassList = generateMigrateTestByClass(apiFilepath, sourceTestFilepathList, fileApis);
            migrateTestList.addAll(migrateTestByClassList);
        }

        //todo 筛选test
//        filterTestMethodWithInvokeRule(migrateTestList);

        List<MigrateTest> mergedMigrateTests = mergeMigrateTest(migrateTestList);

        // print
        reportMigrateTest(mergedMigrateTests);

        return mergedMigrateTests;
    }

    /**
     * 关联api对应的test时，可能不同api会关联出同一个test，为了避免重复迁移同一个test，需要做相同test的merge
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
            // 合并test-api调用关系
            List<String> testMethodApiInvocations = migrateTests.stream().map(MigrateTest::getTestMethodApiInvocation)
                    .collect(Collectors.toList());
            String mergedTestMethodApiInvocation = mergeTestMethodApiInvocation(testMethodApiInvocations);

            // 合并test内部调用情况（简单做了）
            List<String> referenceTypes = migrateTests.stream().map(MigrateTest::getReferenceType)
                    .collect(Collectors.toList());
            String mergedReferenceType = mergeReferenceType(referenceTypes);

            // 构建合并后的MigrateTest
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
        // 对每个test文件做处理
        for (MigrateTest migrateTest : migrateTestList) {
            // 收集test方法被过滤后的map，然后更新
            Map<String, List<Integer>> testMethodApiInvocationMap = JsonUtil.jsonToPojo(migrateTest.getTestMethodApiInvocation(), Map.class);
            Map<String, LinkedHashMap> referenceTypeMap = JsonUtil.jsonToPojo(migrateTest.getReferenceType(), Map.class);

            if (testMethodApiInvocationMap == null) {
                Log.error("filterTestMethodWithInvokeRule testMethodApiInvocationMap不应该为null");
                continue;
            }
            if (referenceTypeMap == null) {
                Log.error("filterTestMethodWithInvokeRule referenceTypeMap不应该为null");
                continue;
            }

            // 对test文件中的每个方法做处理
            Map<String, List<Integer>> filterTestMethodApiInvocationMap = Maps.newHashMap();
            testMethodApiInvocationMap.forEach((methodName, invokeIds) -> {
                LinkedHashMap referenceType = referenceTypeMap.get(methodName);
                if (referenceType == null) {
                    Log.error("filterTestMethodWithInvokeRule referenceType不应该不存在testMethodApiInvocationMap中的方法");
                    // 不过滤
                    filterTestMethodApiInvocationMap.put(methodName, invokeIds);
                    return;
                }

                boolean isFilterTestByRule = filterTestByRule(invokeIds, referenceType);
                // 不过滤的话，test就保留下来
                if (!isFilterTestByRule) {
                    filterTestMethodApiInvocationMap.put(methodName, invokeIds);
                }
            });

            // 改了入参，这里把test过滤后的map进行了更新
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

        //对于api所在文件的filepath，test文件可能不止一个
        String className = CommonBasicService.getClassNameByFilepath(apiFilepath);
        // 找出文件名中包含指定className的test文件
        List<String> testFilepathList = CommonBasicService.filterTestFilepathByClassName(sourceTestFilepathList, className);
//        Log.info("generateMigrateTestByClass - className/testFilepathList: " + className + " / " + Joiner.on(",").join(testFilepathList));
        // 每个测试文件进行处理
        for (String testFilepath : testFilepathList) {
            // 解析测试文件 key:testMethodName, value:invoke api names
            TestMethodParseBasic testMethodParseBasic = parseTestFile(testFilepath);
            if (testMethodParseBasic == null || testMethodParseBasic.getMethodInvocationList() == null) {
                Log.error("buildTestMethodInvokeApiMap error");
                continue;
            }
            // 筛选出方法内部调用到API的test
            Map<String, List<Integer>> testMethodInvocationMap = buildTestMethodInvokeApiMap(testMethodParseBasic, fileApis, className);

            // 用于进一步筛选test
            Map<String, TestMethodParseBasic.ReferenceType> testMethodReferenceMap = buildTestMethodReferenceMap(testMethodParseBasic, fileApis, className);

            // 存在api映射关系，但是找不到目标api合适的测试类，直接跳过
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
     * 分析并筛选出 方法内部调用存在于apiBasic的test
     * 比如存在api：funcA, test方法：test1(){ funcA();} 那么test1就会被收集起来
     */
    private Map<String, List<Integer>> buildTestMethodInvokeApiMap(TestMethodParseBasic testMethodParseBasic,
                                                                   List<ApiBasic> fileApis, String className) {
        // test方法内部的api调用关系map
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
        // 统计 所有test个数，以及存在映射关系的test个数
        int totalInvocationTestNum = 0;
        for (MigrateTest migrateTest : migrateTests) {
            Map<String, List<Integer>> map = JsonUtil.jsonToPojo(migrateTest.getTestMethodApiInvocation(), Map.class);
            if (map == null) {
                continue;
            }
            totalInvocationTestNum += map.keySet().size();
        }
        Log.info("统计存在映射关系的test个数: " + totalInvocationTestNum);
    }

    /**
     * 根据targetSourceCodeFilepath路径找候选test文件
     */
    private List<String> fetchSourceTestFilepathList() {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<String> targetSourceCodeFilepathList = Splitter.on(Constants.SEPARATOR.COMMA)
                .splitToList(taskParameter.getTargetSourceCodeFilepath());

        // FilepathKey不为空，那么过滤出包含该key且为test的文件
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

    /**
     * 解析每个testFile，获取所有testMethod还有对应每个testMethod调用的方法列表（map结构）
     * caller：test方法，callee：test方法中调用的其它方法
     * 解析文件中的test方法，获取每个test方法和该test方法中调用了哪些方法
     * key:testMethodName, value: apiNames
     */
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
