package com.test.migration.tmp;//package com.test.migration;
//
//import com.google.common.base.Joiner;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//import com.test.migration.entity.TaskParameter;
//import com.test.migration.entity.po.ApiBasic;
//import com.test.migration.entity.po.ApiMapping;
//import com.test.migration.entity.po.TranslateTest;
//import com.test.migration.service.ApiBasicService;
//import com.test.migration.service.ApiMappingService;
//import com.test.migration.service.OutputReportCollector;
//import com.test.migration.service.TranslateTestService;
//import lombok.Builder;
//import lombok.Data;
//import org.apache.commons.lang3.StringUtils;
//import utils.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class TestMigrationMain {
//    ApiBasicService apiBasicService = new ApiBasicService();
//    ApiMappingService apiMappingService = new ApiMappingService();
//    TranslateTestService translateTestService = new TranslateTestService();
//
//    public static void main(String[] args) throws Exception {
//        TestMigrationMain main = new TestMigrationMain();
//        // filepath处理
//        //1.指定target父目录，找出所有子目录
//        String sourceFilepath = "/Users/gaoyi/IdeaProjects/staticCodeAnalysis/doc/android/source/platform_frameworks_base";
//        String targetFilepath = "/Users/gaoyi/IdeaProjects/staticCodeAnalysis/doc/harmony/source/arkui_ace_engine-master/frameworks/base";
//        List<String> subFileModuleKeys = main.findSubFileModuleKeys(targetFilepath);
//
//        List<TaskFilepath> taskFilepathList = main.buildTaskFilepathList(sourceFilepath, targetFilepath, subFileModuleKeys);
//
//        // 执行测试迁移
//        taskFilepathList.stream()
//                .filter(taskFilepath -> {
//                    List<String> sourceModuleSourceFilepath = taskFilepath.getSourceModuleSourceFilepath();
//                    List<String> sourceModuleTestFilepath = taskFilepath.getSourceModuleTestFilepath();
//                    return sourceModuleSourceFilepath.size() > 0 && sourceModuleTestFilepath.size() > 0;
//                }).forEach(main::executeTestMigration);
//    }
//
//    private void executeTestMigration(TaskFilepath taskFilepath) {
//        Log.info("executeTestMigration start: ");
//        MappingRuleWriter.clearApiMappingProperties();
//        MappingRuleWriter.clearClassNameMappingProperties();
//
//        // 1.文件路径设置
//        String target = taskFilepath.getTargetModuleFilepath();
//        String source = Joiner.on(",").join(taskFilepath.getSourceModuleSourceFilepath());
//        String test = Joiner.on(",").join(taskFilepath.getSourceModuleTestFilepath());
//        String moduleName = taskFilepath.getModuleName();
//        Log.info("moduleName:"+moduleName);
//        Log.info("source filepath:"+source);
//        Log.info("source test filepath:"+test);
//        Log.info("target filepath:"+target);
//
//        TaskParameterReader.taskParameter = TaskParameterReader.buildTaskParameter(target, source, test, moduleName);
//
//        // 2.执行api mapping过程并输出结果数据
//        ApiMappingMain apiMappingMain = new ApiMappingMain();
//        apiMappingMain.process();
//
//        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(1);
//        Log.info("apiMappings size :" + apiMappings.size());
//        addApiMappingToReport(apiMappings);
//        List<TranslateTest> translateTests = translateTestService.selectByTaskId(1);
//        addCandidateTestToReport(translateTests);
//        OutputReportCollector.output(moduleName);
//
//        // 3.执行test迁移并输出结果数据
//        TranslateMain translateMain = new TranslateMain();
//        translateMain.process();
//        Log.info("executeTestMigration end");
//    }
//
//    /**
//     * 对于每个要进行测试迁移的模块，获取其source/target目录
//     *
//     * @param sourceFilepath
//     * @param targetFilepath
//     * @param subFileModuleKeys
//     * @return
//     * @throws IOException
//     */
//    private List<TaskFilepath> buildTaskFilepathList(String sourceFilepath, String targetFilepath, List<String> subFileModuleKeys) throws IOException {
//        List<String> sourceFilepathList = GetFoldFileNames.readfile(sourceFilepath);
//        // 过滤掉的key
//        List<String> filterKeys = Lists.newArrayList("form", "test");
//        return subFileModuleKeys.stream()
//                .filter(module -> !filterKeys.contains(module))
//
//                // todo 测试代码：跑指定的模块
//                .filter(module -> StringUtils.equals(module, "base64"))
//
//                .map(module -> {
//                    List<String> sourceModuleFilepathList = findAllSourceModuleFilepath(sourceFilepathList, module);
//
//                    // source src
//                    List<String> sourceModuleSourceFilepath = findSourceModuleSourceFilepath(sourceModuleFilepathList);
//                    // source test
//                    List<String> sourceModuleTestFilepath = findSourceModuleTestFilepath(sourceModuleFilepathList);
//                    // target
//                    String targetModuleFilepath = targetFilepath + "/" + module;
//
//                    return TaskFilepath.builder()
//                            .moduleName(module)
//                            .sourceModuleSourceFilepath(sourceModuleSourceFilepath)
//                            .sourceModuleTestFilepath(sourceModuleTestFilepath)
//                            .targetModuleFilepath(targetModuleFilepath)
//                            .build();
//                }).collect(Collectors.toList());
//    }
//
//    /**
//     * 找到target目录的子目录名，设为key
//     *
//     * @param targetFilepath
//     */
//    private List<String> findSubFileModuleKeys(String targetFilepath) {
//        File file = new File(targetFilepath);
//        List<String> targetModuleKeys = Lists.newArrayList();
//        if (file.isDirectory()) {
//            List<String> subFiles = Arrays.stream(Objects.requireNonNull(file.list()))
//                    .collect(Collectors.toList());
//            targetModuleKeys.addAll(subFiles);
//        }
//
//        return targetModuleKeys.stream()
//                .distinct()
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * 在source中找到包含module key的目录
//     *
//     * @param sourceFilepathList
//     * @param module
//     * @return
//     */
//    public List<String> findAllSourceModuleFilepath(List<String> sourceFilepathList, String module) {
//        return sourceFilepathList.stream()
//                .map(filepath -> {
//                    String[] split = filepath.split("/");
//                    List<String> collect = Arrays.stream(split).collect(Collectors.toList());
//
//                    StringBuilder stringBuilder = new StringBuilder();
//                    for (int i = 0; i < collect.size() - 1; i++) {
//                        stringBuilder.append(collect.get(i)).append("/");
//                    }
//
//                    // 目录名中包含module
//                    String dirFilepath = stringBuilder.toString();
//                    if (dirFilepath.contains(module)) {
//                        return dirFilepath;
//                    }
//
//                    // 文件名中包含module
//                    String fileName = collect.get(collect.size() - 1);
//                    if (fileName.toLowerCase().contains(module)) {
//                        return (dirFilepath + "/" + fileName).replace("//", "/");
//                    }
//
//                    return null;
//                })
//                .filter(Objects::nonNull)
//                .distinct()
//                .collect(Collectors.toList());
//    }
//
//    public List<String> findSourceModuleSourceFilepath(List<String> sourceModuleFilepathList) {
//        return sourceModuleFilepathList.stream()
//                .filter(x -> !x.toLowerCase().contains("test"))
//                .collect(Collectors.toList());
//
//    }
//
//    public List<String> findSourceModuleTestFilepath(List<String> sourceModuleFilepathList) {
//        return sourceModuleFilepathList.stream()
//                .filter(x -> x.toLowerCase().contains("test"))
//                .collect(Collectors.toList());
//    }
//
//    @Data
//    @Builder
//    public static class TaskFilepath {
//        String moduleName;
//
//        List<String> sourceModuleSourceFilepath;
//        // source test
//        List<String> sourceModuleTestFilepath;
//        // target
//        String targetModuleFilepath;
//    }
//
//
//    private void addCandidateTestToReport(List<TranslateTest> translateTests) {
//        if (translateTests.isEmpty()) {
//            return;
//        }
//        OutputReportCollector.add("关联出待迁移Test的详细信息：");
//        OutputReportCollector.add("待迁移test文件总数：" + translateTests.size());
//
//        Set<String> methodNameSet = Sets.newHashSet();
//        for (TranslateTest test : translateTests) {
//            OutputReportCollector.add("test文件路径：" + test.getTestFilepath());
//            OutputReportCollector.add("包含的test方法：");
//
//            Map<String, List<Integer>> map = JsonUtil.jsonToPojo(test.getTestMethodApiInvocation(), Map.class);
//            if (map == null) {
//                continue;
//            }
//            methodNameSet.addAll(map.keySet());
////            map.keySet().forEach(OutputReportCollector::add);
//        }
//
//        OutputReportCollector.add("待迁移test方法总数：" + methodNameSet.size());
//
//    }
//
//    private void addApiMappingToReport(List<ApiMapping> apiMappings) {
//        if (apiMappings.isEmpty()) {
//            return;
//        }
//
//        int taskId = 1;
//        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(taskId);
//        Map<Integer, ApiBasic> apiBasicMap =
//                apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));
//        OutputReportCollector.add("API mapping详细信息：");
//        OutputReportCollector.add("mapping总数：" + apiMappings.size());
//        OutputReportCollector.add("mapping(源类名-API ：目标类名-API)：");
//        for (ApiMapping apiMapping : apiMappings) {
//            ApiBasic sourceApi = apiBasicMap.get(apiMapping.getSourceApiId());
//            ApiBasic targetApi = apiBasicMap.get(apiMapping.getTargetApiId());
//            OutputReportCollector.add(sourceApi.getClassName() + " - " + sourceApi.getApiName() + " : " + targetApi.getClassName() + " - " + targetApi.getApiName());
//        }
//    }
//}
