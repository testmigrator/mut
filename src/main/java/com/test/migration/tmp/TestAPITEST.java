package com.test.migration.tmp;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import com.test.migration.entity.po.MigrateTest;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.MigrateTestService;
import com.test.migration.service.TranslateTestService;
import utils.FileReadUtil;
import utils.FileWriteUtil;
import utils.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestAPITEST {
    static ApiBasicService apiBasicService = new ApiBasicService();
    static ApiMappingService apiMappingService = new ApiMappingService();
    static MigrateTestService migrateTestService = new MigrateTestService();

    public static void main(String[] args) throws Exception {
        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
        Map<Integer, ApiBasic> apiBasicMap = apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));
        List<ApiBasic> apiBasicsWithoutNonApis = apiBasics.stream()
                .filter(x -> x.getType() != 3 && x.getType() != 4)
                .collect(Collectors.toList());
        List<String> sourceApis = apiBasicsWithoutNonApis.stream()
                .filter(x -> x.getType() == 0)
//                .map(x -> x.getApiName() + "      #" + x.getClassName())
                .map(x -> x.getApiName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        List<String> targetApis = apiBasicsWithoutNonApis.stream()
                .filter(x -> x.getType() == 1)
//                .map(x -> x.getApiName() + "      #" + x.getClassName())
                .map(x -> x.getApiName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(1);
        List<String> mappings = apiMappings.stream()
                .map(x -> {
                    ApiBasic sourceApi = apiBasicMap.get(x.getSourceApiId());
                    ApiBasic targetApi = apiBasicMap.get(x.getTargetApiId());
                    return x.getId() + "," + sourceApi.getApiName() + "   " + sourceApi.getClassName() + "    |    " + targetApi.getApiName() + "   " + targetApi.getClassName();
                })
                .distinct()
                .collect(Collectors.toList());

        List<MigrateTest> migrateTests = migrateTestService.selectByTaskId(1);
        Set<Integer> allMappingApiIds = Sets.newHashSet();

        for (MigrateTest x : migrateTests) {
            Map<String, List<Integer>> map = JsonUtil.jsonToPojo(x.getTestMethodApiInvocation(), Map.class);
            if (map == null) {
                continue;
            }
            for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
                List<Integer> value = entry.getValue();
                allMappingApiIds.addAll(value);
            }
        }

        List<ApiMapping> existInvokeMappings = apiMappings.stream()
                .filter(x -> allMappingApiIds.contains(x.getSourceApiId()))
                .collect(Collectors.toList());

        List<String> existInvokeMappingsStr = existInvokeMappings.stream()
                .map(x -> {
                    ApiBasic sourceApi = apiBasicMap.get(x.getSourceApiId());
                    ApiBasic targetApi = apiBasicMap.get(x.getTargetApiId());
                    return x.getId() + "," + sourceApi.getApiName() + "   " + sourceApi.getClassName() + "    |    " + targetApi.getApiName() + "   " + targetApi.getClassName();
                })
                .distinct()
                .collect(Collectors.toList());


        //构建每个api，对应哪些test
        // api mapping
        // testfilepath test repeat
        List<String> testLines = Lists.newArrayList();
        List<String> testLinesWithoutRepeat = testLinesWithoutRepeat(apiBasicMap, migrateTests, existInvokeMappings, testLines);

        String sourceApiFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/source_apis.txt";
        String targetApiFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/target_apis.txt";
        String mappingsFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/mapping_apis_all.txt";
        String existInvokeMappingsFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/mapping_apis_existInvoke.txt";
        String testInvokeFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/test_invoke.txt";
        String testInvokeWithoutRepeatFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/test_invoke_WithoutRepeat.txt";
        FileWriteUtil.writeDataToFileWithoutAppend(sourceApis, sourceApiFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(targetApis, targetApiFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(mappings, mappingsFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(existInvokeMappingsStr, existInvokeMappingsFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(testLines, testInvokeFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(testLinesWithoutRepeat, testInvokeWithoutRepeatFilepath);
    }

    /**
     * api调整后，重新筛选test
     */
    public static class ReInvokeTest {

        public static void main(String[] args) throws Exception {
            List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
            Map<Integer, ApiBasic> apiBasicMap = apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));
            List<MigrateTest> migrateTests = migrateTestService.selectByTaskId(1);
            List<ApiMapping> reApiMappings = filterApiMappings();
            List<String> testLines = Lists.newArrayList();
            List<String> reLists = testLinesWithoutRepeat(apiBasicMap, migrateTests, reApiMappings, testLines);

            String reTestInvokeFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/test_invoke_Re.txt";
            FileWriteUtil.writeDataToFileWithoutAppend(reLists, reTestInvokeFilepath);
        }

        private static List<ApiMapping> filterApiMappings() throws IOException {
            String keepFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/mapping_apis_modify.txt";
            List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(1);
            List<String> keepLines = FileReadUtil.readFile(keepFilepath);
            List<Integer> keepIds = keepLines.stream()
                    .map(line -> {
                        System.out.println(line);
                        return Integer.parseInt(line.split(",")[0]);
                    })
                    .collect(Collectors.toList());

            return apiMappings.stream()
                    .filter(x -> keepIds.contains(x.getId()))
                    .collect(Collectors.toList());
        }
    }

    private static List<String> testLinesWithoutRepeat(Map<Integer, ApiBasic> apiBasicMap, List<MigrateTest> migrateTests,
                                                       List<ApiMapping> existInvokeMappings, List<String> testLines) {
        Set<String> tmps = Sets.newHashSet();
        for (ApiMapping existInvokeMapping : existInvokeMappings) {
            ApiBasic sourceApi = apiBasicMap.get(existInvokeMapping.getSourceApiId());
            ApiBasic targetApi = apiBasicMap.get(existInvokeMapping.getTargetApiId());
            testLines.add(existInvokeMapping.getId() + "," + sourceApi.getApiName() + "   " + sourceApi.getClassName() + "    |    " + targetApi.getApiName() + "   " + targetApi.getClassName());

            // translateTests：筛选出的所有待迁移test
            // 这一步要对上述test进行过滤，test必须调用在mapping_apis_modify中的source-api
            for (MigrateTest x : migrateTests) {
                String testFilepath = x.getTestFilepath();
                // key：testName value：api_ids
                Map<String, List<Integer>> map = JsonUtil.jsonToPojo(x.getTestMethodApiInvocation(), Map.class);
                if (map == null) {
                    continue;
                }

                for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
                    String testName = entry.getKey();
                    List<Integer> invokeApiIds = entry.getValue();

//                    if(testName.equals("test_values") &&
//                            testFilepath.equals("/Users/gaoyi/IdeaProjects/staticCodeAnalysis/doc/dataset/collection/jdk8/jdk8u_tests/harmony/java/trunk/classlib/modules/luni/src/test/api/common/org/apache/harmony/luni/tests/java/util/LinkedHashMapTest.java")){
//                        if(invokeApiIds.contains(existInvokeMapping.getSourceApiId())){
//                            System.out.println(
//                                    "WARN: "+existInvokeMapping.getSourceApiId()
//                            );
//                        }
//                    }


                    if (invokeApiIds.contains(existInvokeMapping.getSourceApiId())) {
                        if (tmps.contains(testFilepath + "$" + testName)) {
                            testLines.add(testFilepath + "$" + testName + "     $repeat");
                        } else {
                            tmps.add(testFilepath + "$" + testName);
                            testLines.add(testFilepath + "$" + testName);
                        }
                    }
                }
            }
        }
        return testLines.stream()
                .filter(x -> !x.contains("$repeat"))
                .collect(Collectors.toList());
    }

}
