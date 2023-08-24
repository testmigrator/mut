package com.test.migration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import com.test.migration.entity.po.MigrateTest;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.MigrateTestService;
import com.test.migration.service.TranslateTestService;
import utils.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatasetSelectMain {

    static final String OUTPUT_FILE_PATH = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/batch/";
    static final String DATASET_FILE_PATH = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/dataset/";
    static final String dataKey = "htmlxml";

    static ApiMappingMain apiMappingMain = new ApiMappingMain();
    static MigrateTestMain migrateTestMain = new MigrateTestMain();
    static ApiBasicService apiBasicService = new ApiBasicService();
    static ApiMappingService apiMappingService = new ApiMappingService();
    static MigrateTestService migrateTestService = new MigrateTestService();

    public static void main(String[] args) throws Exception {
        String sourceDataset = DATASET_FILE_PATH + dataKey + "/sourceDataset.txt";
        String sourceTestDataset = DATASET_FILE_PATH + dataKey + "/sourceTestDataset.txt";
        String targetDataset = DATASET_FILE_PATH + dataKey + "/targetDataset.txt";

        List<String> sourceFilepathList = FileReadUtil.readFile(sourceDataset);
        List<String> sourceTestFilepathList = FileReadUtil.readFile(sourceTestDataset);
        List<String> targetFilepathList = FileReadUtil.readFile(targetDataset);

        batchSelect(sourceFilepathList, sourceTestFilepathList, targetFilepathList);
    }

    public static void batchSelect(List<String> sourceFilepathList,
                                   List<String> sourceTestFilepathList,
                                   List<String> targetFilepathList) throws Exception {
        //1.source
        int sourceLen = sourceFilepathList.size();
        int targetLen = targetFilepathList.size();

        for (int i = 0; i < sourceLen; i++) {
            String sourceFilepath = sourceFilepathList.get(i);
            String sourceTestFilepath = sourceTestFilepathList.get(i);
            for (int j = 0; j < targetLen; j++) {
                String targetFilepath = targetFilepathList.get(j);
                TaskParameterReader.taskParameter = TaskParameterReader.buildTaskParameter(sourceFilepath, targetFilepath, sourceTestFilepath);

                apiMappingMain.process();
                migrateTestMain.process();

                String dataIndex = "data-" + i + "-" + j;
                reportMapping(dataIndex);
            }
        }
    }


    public static void reportMapping(String dataIndex) throws Exception {
        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);

        Map<Integer, ApiBasic> apiBasicMap = apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));

        List<ApiBasic> apiBasicsWithoutNonApis = apiBasics.stream()
                .filter(x -> x.getType() != 3 && x.getType() != 4)
                .collect(Collectors.toList());

        List<String> sourceApis = apiBasicsWithoutNonApis.stream()
                .filter(x -> x.getType() == 0)
                .map(ApiBasic::getApiName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> targetApis = apiBasicsWithoutNonApis.stream()
                .filter(x -> x.getType() == 1)
                .map(ApiBasic::getApiName)
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


        // api mapping
        // testfilepath test repeat

        List<String> testLines = Lists.newArrayList();
        List<String> testLinesWithoutRepeat = testLinesWithoutRepeat(apiBasicMap, migrateTests, existInvokeMappings, testLines);

        File file = new File(OUTPUT_FILE_PATH + dataIndex);
        file.mkdir();

        String sourceApiFilepath = OUTPUT_FILE_PATH + dataIndex + File.separator + "source_apis.txt";
        String targetApiFilepath = OUTPUT_FILE_PATH + dataIndex + File.separator + "target_apis.txt";
        String mappingsFilepath = OUTPUT_FILE_PATH + dataIndex + File.separator + "mapping_apis_all.txt";
        String existInvokeMappingsFilepath = OUTPUT_FILE_PATH + dataIndex + File.separator + "mapping_apis_existInvoke.txt";
        String testInvokeFilepath = OUTPUT_FILE_PATH + dataIndex + File.separator + "test_invoke.txt";
        String testInvokeWithoutRepeatFilepath = OUTPUT_FILE_PATH + dataIndex + File.separator + "test_invoke_WithoutRepeat.txt";

        FileWriteUtil.writeDataToFileWithoutAppend(sourceApis, sourceApiFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(targetApis, targetApiFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(mappings, mappingsFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(existInvokeMappingsStr, existInvokeMappingsFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(testLines, testInvokeFilepath);
        FileWriteUtil.writeDataToFileWithoutAppend(testLinesWithoutRepeat, testInvokeWithoutRepeatFilepath);
    }

    private static List<String> testLinesWithoutRepeat(Map<Integer, ApiBasic> apiBasicMap, List<MigrateTest> migrateTests,
                                                       List<ApiMapping> existInvokeMappings, List<String> testLines) {
        Set<String> tmps = Sets.newHashSet();
        for (ApiMapping existInvokeMapping : existInvokeMappings) {
            ApiBasic sourceApi = apiBasicMap.get(existInvokeMapping.getSourceApiId());
            ApiBasic targetApi = apiBasicMap.get(existInvokeMapping.getTargetApiId());
            testLines.add(existInvokeMapping.getId() + "," + sourceApi.getApiName() + "   " + sourceApi.getClassName() + "    |    " + targetApi.getApiName() + "   " + targetApi.getClassName());

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
