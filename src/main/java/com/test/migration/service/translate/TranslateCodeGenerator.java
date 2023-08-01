package com.test.migration.service.translate;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.test.migration.entity.Constants;
import com.test.migration.entity.TaskParameter;
import com.test.migration.entity.po.TestMethodDiff;
import com.test.migration.service.TestMethodDiffService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import utils.CallUtil;
import utils.FileWriteUtil;
import utils.Log;
import utils.TaskParameterReader;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 收集转换后的testCode，两个类别：
 * 1.testCode类成员变量：属性，内部类，非测试方法；其中每一个都带有对应的hint
 * <p>
 * 2.测试方法
 * 方法由blockStatement组成，测试方法用一组[blockStatement,hint]组成
 */
public class TranslateCodeGenerator {

    private static TestMethodDiffService testMethodDiffService = new TestMethodDiffService();

    //声明一个线程池
//    static ExecutorService executor = new ThreadPoolExecutor(5, 5,
//            20L, TimeUnit.SECONDS,
//            new SynchronousQueue<Runnable>());
//    //超时控制
//    static SimpleTimeLimiter simpleTimeLimiter = SimpleTimeLimiter.create(executor);
//

    public static void doGenerate() {
        Log.info("开始执行代码生成");

        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();
        File file = new File(taskParameter.getOutputFilepath());
        if(!file.exists()){
            boolean mkdir = new File(taskParameter.getOutputFilepath()).mkdir();
        }

        String filepath = taskParameter.getOutputFilepath() + TranslateCodeCollector.className + ".cpp";

        //将TranslateCodeCollector的结果整理并输出到一个文件中
        Log.info("将TranslateCodeCollector的结果整理并输出到一个文件中");
        writeMigrationResultToFile(filepath);
//        formatMigrationResultFile(filepath);

        //代码格式化 调用的工具可能陷入死循环，设置超时报错
//        try {
//            simpleTimeLimiter.runWithTimeout(() -> {
//                formatMigrationResultFile(filepath);
//            }, Duration.ofSeconds(10));
//        } catch (Exception e) {
//            Log.error("输出文件代码格式化失败,filepath: " + filepath);
//        }
//
        Log.info("代码生成执行完毕");
    }

    /**
     * 将TranslateCodeCollector的结果整理并输出到一个文件中
     */
    public static void writeMigrationResultToFile(String filepath) {
        System.out.println("writeMigrationResultToFile start");
        List<String> fileLines = Lists.newArrayList();
        fileLines.add("#include \"gtest/gtest.h\"");
        fileLines.add("using namespace testing;");
        fileLines.add("using namespace testing::ext;");
        fileLines.add("class " + TranslateCodeCollector.className + " : public testing::Test {");
        fileLines.add("public:");

        // 成员属性，内部类，测试方法
        fillTranslateCodeToFileLines(fileLines);

        // 测试类结尾
        fileLines.add(" } ");

        // 写入文件
        try {
            FileWriteUtil.writeDataToFile(fileLines, filepath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("writeMigrationResultToFile end");
    }

    private static void fillTranslateCodeToFileLines(List<String> fileLines) {
        // 写入类成员属性
        for (TranslateCodeCollector.TranslateCode translateCode : TranslateCodeCollector.fieldDeclarationTranslateCodes) {
            List<String> hints = addHintIfNeed(translateCode.misMatchCodes);
            fileLines.addAll(hints);
            String missMappingTips = Joiner.on(" ").join(hints);
            fileLines.add(translateCode.translateCode);
            // 换行
            fileLines.add("");

            updateTestMethodDiff(translateCode.startLine, missMappingTips, translateCode.translateCode);
        }

        // 写入内部类
        for (TranslateCodeCollector.TranslateCode translateCode : TranslateCodeCollector.classDeclarationTranslateCodes) {
            List<String> hints = addHintIfNeed(translateCode.misMatchCodes);
            fileLines.addAll(hints);
            String missMappingTips = Joiner.on(" ").join(hints);
            fileLines.add("class " + translateCode.translateCode);
            // 换行
            fileLines.add("");
            updateTestMethodDiff(translateCode.startLine, missMappingTips, translateCode.translateCode);
        }

        // 写入方法
        for (TranslateCodeCollector.MethodTranslateCode methodTranslateCode : TranslateCodeCollector.methodDeclarationTranslateCodes) {
            List<String> newCodeLines = Lists.newArrayList();
            List<String> methodHints = addMethodHints(methodTranslateCode);

            fileLines.addAll(methodHints);
            String missMappingTips = Joiner.on(" ").join(methodHints);

            newCodeLines.add(methodTranslateCode.methodHeaderTranslateCode.translateCode);
            fileLines.add(methodTranslateCode.methodHeaderTranslateCode.translateCode);

            newCodeLines.add(" { ");
            fileLines.add(" { ");
            for (TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode blockStatementTranslateCode
                    : methodTranslateCode.blockStatementTranslateCodes) {
                newCodeLines.add(blockStatementTranslateCode.translateCode);
                fileLines.add(blockStatementTranslateCode.translateCode);
            }
            newCodeLines.add(" } ");
            fileLines.add(" } ");
            // 换行
            fileLines.add("");

            String newCode = Joiner.on("\n").join(newCodeLines);
            updateTestMethodDiff(methodTranslateCode.startLine, missMappingTips, newCode);
        }

        // 写入部分迁移方法
        fileLines.add("// 部分迁移结果：");
        for (TranslateCodeCollector.PartMigrationMethodTranslateCode translateCode : TranslateCodeCollector.partMigrationMethodTranslateCodes) {
            fileLines.add(translateCode.translateCode);
        }
    }

    private static void updateTestMethodDiff(int startLine, String missMappingTips, String newCode) {
        String filepath = TranslateCodeCollector.filepath;
        String businessKey = filepath+"$"+startLine;

        TestMethodDiff testMethodDiff = TestMethodDiff.builder()
                .businessKey(businessKey)
                .newCode(newCode)
                .missMappingTips(missMappingTips)
                .build();
        testMethodDiffService.update(testMethodDiff);
    }

    private static List<String> addMethodHints(TranslateCodeCollector.MethodTranslateCode methodTranslateCode) {
        List<String> methodHints = Lists.newArrayList();
        String headerHint = formatHintLine(methodTranslateCode.methodHeaderTranslateCode.misMatchCodes);
        methodHints.add(headerHint);
        for (TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode blockStatementTranslateCode
                : methodTranslateCode.blockStatementTranslateCodes) {
            String blockStatementHint = formatHintLine(blockStatementTranslateCode.misMatchCodes);
            methodHints.add(blockStatementHint);
        }

        // 去重
        methodHints = methodHints.stream()
                .filter(StringUtils::isNotBlank)
                .map(x -> x.replaceAll("//", ""))
                .distinct()
                .collect(Collectors.toList());
        String join = Joiner.on(",").join(methodHints);
        List<String> methodHintDistinct = Splitter.on(",").splitToList(join)
                .stream().distinct()
                .collect(Collectors.toList());
        List<String> hints = Lists.newArrayList();

        // 新增hint
        String methodBodyHint = "// " + Joiner.on(",").join(methodHintDistinct);
        hints.add(Constants.Hint.missingMappingMessage);
        hints.add(methodBodyHint);
        return hints;
    }

    /**
     * 如果有hint，那么添加代码注释，如果没有hint就return
     */
    private static List<String> addHintIfNeed(List<String> translateCode) {
        List<String> hints = Lists.newArrayList();
        String formatHintLine = formatHintLine(translateCode);
        if (StringUtils.isNotBlank(formatHintLine)) {
            hints.add(Constants.Hint.missingMappingMessage);
            hints.add(formatHintLine);
        }
        return hints;
    }

    /**
     * 将输出的文件进行代码格式化处理
     */
    public static void formatMigrationResultFile(String filepath) {
        String cmd = "clang-format " + filepath;
        List<String> lines = CallUtil.callCMD(cmd);

        try {
            new File(filepath).deleteOnExit();
            String[] fileStr = filepath.split(File.separator);

            String oldFileName = fileStr[fileStr.length - 1];
            String newFileName = "test_" + oldFileName.toLowerCase();
            fileStr[fileStr.length - 1] = newFileName;

            List<String> filePathStr = Arrays.stream(fileStr).collect(Collectors.toList());
            String formatFilepath = Joiner.on(File.separator).join(filePathStr);

            FileWriteUtil.writeDataToFile(lines, formatFilepath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String formatHintLine(List<String> misMatchCodes) {
        if (CollectionUtils.isEmpty(misMatchCodes)) {
            return StringUtils.EMPTY;
        }
        return "//" + Joiner.on(",").join(misMatchCodes);
    }


}
