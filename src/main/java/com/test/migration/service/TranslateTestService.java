package com.test.migration.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.dao.MigrateTestDao;
import com.test.migration.entity.Constants;
import com.test.migration.entity.TaskParameter;
import com.test.migration.entity.po.MigrateTest;
import com.test.migration.entity.po.TestMethodDiff;
import com.test.migration.service.translate.*;
import com.test.migration.service.translate.bnf.common.method.MethodDeclarationTranslate;
import com.test.migration.service.translate.bnf.declaration.ClassDeclarationTranslate;
import com.test.migration.service.translate.bnf.declaration.FieldDeclarationTranslate;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import utils.JsonUtil;
import utils.Log;
import utils.MyBatisUtil;
import utils.TaskParameterReader;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslateTestService {
    private final TestMethodDiffService testMethodDiffService = new TestMethodDiffService();
    private final MigrateTestService migrateTestService = new MigrateTestService();

    /**
     * 代码转换：
     * 候选ut文件，按照ut中需要做迁移的test method，以文件为单位进行转换
     */
    public void translateCode() {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<MigrateTest> migrateTests = migrateTestService.selectByTaskId(taskParameter.getTaskId());

        // 加载代码匹配关系
        MappingRuleLoader.load();

        Log.info("总计[" + migrateTests.size() + "]个测试文件需要处理代码迁移");

        // 以每个test文件为单位进行代码转换
        migrateTests.stream()
                //todo test 排查case
//                .filter(x->x.getTestFilepath().contains("TestDateTimeFormatter"))
                .forEach(this::doTranslate);
    }

    private void doTranslate(MigrateTest migrateTest) {
        Log.info("开始处理[" + migrateTest.getTestFilepath() + "]测试文件的测试迁移");

        String testFilepath = migrateTest.getTestFilepath();
        String testMethodApiInvocation = migrateTest.getTestMethodApiInvocation();

        fillTestCodeContext(testFilepath);

        // 过滤需要转换的test method
        filterNeedTranslateTestMethod(testMethodApiInvocation);

        // 代码转换
        doTranslate();

        // 代码生成 TODO test diff可视化 不需要生成文件
        TranslateCodeGenerator.doGenerate();
    }

    private static void fillTestCodeContext(String testFilepath) {
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(testFilepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Java8Parser parser = new Java8Parser(new CommonTokenStream(new Java8Lexer(inputStream)));
        ParseTree parseTree = parser.compilationUnit();

        TestCodeContext.init();
        TestCodeContext.filepath = testFilepath;
        TestCodeVisitor testCodeVisitor = new TestCodeVisitor();
        testCodeVisitor.visit(parseTree);

        Log.info("当前处理的测试类名：" + TestCodeContext.className);
    }

    private static void filterNeedTranslateTestMethod(String testMethodApiInvocation) {
        Map<String, List<Integer>> map = JsonUtil.jsonToPojo(testMethodApiInvocation, Map.class);
        List<String> migrateTestMethods = map == null ? Lists.newArrayList() : Lists.newArrayList(map.keySet());

        // 过滤掉不需要转换的test code
        TestCodeFilter.filterMethodDeclarationCtxList(migrateTestMethods);
    }

    private void doTranslate() {
        TranslateCodeCollector.init();
        TranslateCodeCollector.className = TestCodeContext.className;
        TranslateCodeCollector.filepath = TestCodeContext.filepath;

        // 类迁移
        translateClassDeclaration();

        // 成员变量迁移
        translateFieldDeclaration();

        // 清除上面已经收集过的bs
        TranslateCodeCollector.blockStatementTranslateCodes = Lists.newArrayList();

        // 方法迁移
        translateFullMethod();

        // 方法部分迁移 TODO:已经可以用chatgpt来做测试解耦合了，部分迁移先不做
//        translatePartMethod();

        Log.info("代码转换完成");
    }

    private void translatePartMethod() {
        TranslateCodeCollector.isFullTranslate = false; // 。。。
        PartMigrationProcessor partMigrationProcessor = new PartMigrationProcessor();
        for (ParserRuleContext parserRuleContext : TestCodeContext.methodDeclarationCtxList) {
            TranslateCodeCollector.PartMigrationMethodTranslateCode partMigrationMethodTranslateCode = new TranslateCodeCollector.PartMigrationMethodTranslateCode();
            String translateCode = partMigrationProcessor.doPartMigrationTranslate(parserRuleContext);
            if (StringUtils.isBlank(translateCode)) {
                continue;
            }
            partMigrationMethodTranslateCode.translateCode = translateCode;
            TranslateCodeCollector.partMigrationMethodTranslateCodes.add(partMigrationMethodTranslateCode);
        }
    }

    private void translateFullMethod() {
        MethodDeclarationTranslate methodDeclarationTranslate = new MethodDeclarationTranslate();

        for (ParserRuleContext parserRuleContext : TestCodeContext.methodDeclarationCtxList) {
            TestMethodDiff testMethodDiff = new TestMethodDiff();

            testMethodDiffService.fillTestMethodDiffCommon(parserRuleContext, testMethodDiff);
            testMethodDiffService.fillTestMethodDiffMethodName(parserRuleContext, testMethodDiff);

            TranslateHint.init();
            TranslateCodeCollector.MethodTranslateCode.methodStartLine = parserRuleContext.getStart().getLine();
            TranslateCodeCollector.MethodTranslateCode.methodEndLine = parserRuleContext.getStop().getLine();

            methodDeclarationTranslate.translateMethodDeclaration(parserRuleContext);

            // methodHeader信息
            // method blockStatement信息
            TranslateCodeCollector.MethodTranslateCode methodTranslateCode = new TranslateCodeCollector.MethodTranslateCode();
            methodTranslateCode.methodHeaderTranslateCode = TranslateCodeCollector.methodHeaderTranslateCode;
            // 过滤掉嵌套的blockStatement（可能重复收集了子blockStatement）
            methodTranslateCode.blockStatementTranslateCodes = filterRepeatSubBlockStatement(TranslateCodeCollector.blockStatementTranslateCodes);
            methodTranslateCode.startLine = parserRuleContext.getStart().getLine();

            TranslateCodeCollector.methodDeclarationTranslateCodes.add(methodTranslateCode);

            // 一对多 类匹配 替换
            testMethodDiffService.fillTestMethodDiffClassMapping(methodTranslateCode, testMethodDiff);
            testMethodDiffService.save(testMethodDiff);

            TranslateCodeCollector.MethodTranslateCode.clearMethod();
        }
    }

    private void translateFieldDeclaration() {
        FieldDeclarationTranslate fieldDeclarationTranslate = new FieldDeclarationTranslate();

        for (ParserRuleContext parserRuleContext : TestCodeContext.fieldDeclarationCtxList) {
            TestMethodDiff testMethodDiff = new TestMethodDiff();
            testMethodDiffService.fillTestMethodDiffCommon(parserRuleContext, testMethodDiff);
            testMethodDiffService.fillTestMethodNameAndType(parserRuleContext,
                    Constants.CodeDiffType.FieldDeclaration,
                    testMethodDiff);

            TranslateHint.init();
            TranslateCodeCollector.TranslateCode translateCode = new TranslateCodeCollector.TranslateCode();
            translateCode.translateCode = fieldDeclarationTranslate.translateFieldDeclaration(parserRuleContext);
            translateCode.misMatchCodes = TranslateHint.formatMisMatchCodes(TranslateHint.misMatchCodes);
            translateCode.startLine = parserRuleContext.getStart().getLine();

            testMethodDiffService.fillTestMethodDiffClassMapping(translateCode, testMethodDiff);
            testMethodDiffService.save(testMethodDiff);

            TranslateCodeCollector.fieldDeclarationTranslateCodes.add(translateCode);
        }
    }

    private void translateClassDeclaration() {
        ClassDeclarationTranslate classDeclarationTranslate = new ClassDeclarationTranslate();

        for (ParserRuleContext parserRuleContext : TestCodeContext.classDeclarationCtxList) {
            TestMethodDiff testMethodDiff = new TestMethodDiff();
            testMethodDiffService.fillTestMethodDiffCommon(parserRuleContext, testMethodDiff);
            testMethodDiffService.fillTestMethodNameAndType(parserRuleContext,
                    Constants.CodeDiffType.ClassDeclaration,
                    testMethodDiff);

            TranslateHint.init();
            TranslateCodeCollector.TranslateCode translateCode = new TranslateCodeCollector.TranslateCode();
            translateCode.translateCode = classDeclarationTranslate.translateClassDeclaration(parserRuleContext);
            translateCode.misMatchCodes = TranslateHint.formatMisMatchCodes(TranslateHint.misMatchCodes);
            translateCode.startLine = parserRuleContext.getStart().getLine();

            testMethodDiffService.fillTestMethodDiffClassMapping(translateCode, testMethodDiff);
            testMethodDiffService.save(testMethodDiff);

            TranslateCodeCollector.classDeclarationTranslateCodes.add(translateCode);
        }
    }

    /**
     * bs可能出现嵌套，内部的bs过滤掉
     *
     * @param blockStatementTranslateCodes
     * @return
     */
    private List<TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode> filterRepeatSubBlockStatement(
            List<TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode> blockStatementTranslateCodes) {

        // 1. 按bs（blockStatement）的起始token序号大小自然排序
        List<TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode> sortedBlockStatementTranslateCodes =
                blockStatementTranslateCodes.stream()
                        .sorted(Comparator.comparing(TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode::getTokenStartIndex))
                        .collect(Collectors.toList());

        Set<Integer> removedBlockStatementTokenStartIndexSet = Sets.newHashSet();
        for (int i = 0; i < sortedBlockStatementTranslateCodes.size(); i++) {
            TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode blockStatementTranslateCode = sortedBlockStatementTranslateCodes.get(i);

            if (removedBlockStatementTokenStartIndexSet.contains(blockStatementTranslateCode.getTokenStartIndex())) {
                continue;
            }

            for (int j = i + 1; j < sortedBlockStatementTranslateCodes.size(); j++) {
                TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode subBlockStatementTranslateCode = sortedBlockStatementTranslateCodes.get(j);
                int bsStartIndex = blockStatementTranslateCode.getTokenStartIndex();
                int bsEndIndex = blockStatementTranslateCode.getTokenStopIndex();
                int subBsStartIndex = subBlockStatementTranslateCode.getTokenStartIndex();
                int subBsEndIndex = subBlockStatementTranslateCode.getTokenStopIndex();

                if (subBsStartIndex > bsStartIndex && subBsEndIndex < bsEndIndex) {
                    removedBlockStatementTokenStartIndexSet.add(subBsStartIndex);
                }
            }
        }

        return blockStatementTranslateCodes.stream()
                .filter(x -> !removedBlockStatementTokenStartIndexSet.contains(x.getTokenStartIndex()))
                .collect(Collectors.toList());
    }

}
