package com.test.migration.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.dao.TestMethodDiffDao;
import com.test.migration.entity.Constants;
import com.test.migration.entity.TaskParameter;
import com.test.migration.entity.enums.TestMethodStatus;
import com.test.migration.entity.po.TestMethodDiff;
import com.test.migration.service.preprocess.TestMethodBasicService;
import com.test.migration.service.translate.MappingRuleLoader;
import com.test.migration.service.translate.TestCodeContext;
import com.test.migration.service.translate.TranslateCodeCollector;
import com.test.migration.service.translate.bnf.declaration.ClassDeclarationTranslate;
import com.test.migration.service.translate.replace.ClassMappingReplace;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TestMethodDiffService {

    public void translateCoupleCode(String businessKey) {
        MappingRuleLoader.load();
        TranslateCodeCollector.init();

        TestMethodDiff byBusinessKey = getByBusinessKey(businessKey);

        if (byBusinessKey == null || StringUtils.isBlank(byBusinessKey.getDecoupleOldCode())) {
            return;
        }

        try {
            String newCoupleCode = translateOldCoupleCode(byBusinessKey);

            // 代码格式化
            String formatNewCode = formatCode(newCoupleCode);

            TestMethodDiff testMethodDiff = TestMethodDiff.builder()
                    .businessKey(businessKey)
                    .decoupleNewCode(formatNewCode).build();
            update(testMethodDiff);

        } catch (Exception e) {
            e.printStackTrace();
            Log.error("translateCoupleCode报错啦！businessKey:" + businessKey);
        }
    }

    private static String translateOldCoupleCode(TestMethodDiff byBusinessKey) {
        CharStream inputStream;
        inputStream = CharStreams.fromString(byBusinessKey.getDecoupleOldCode());
        Java8Parser parser = new Java8Parser(new CommonTokenStream(new Java8Lexer(inputStream)));
        Java8Parser.ClassDeclarationContext parseTree = parser.classDeclaration();

        TestCodeContext.init();
        ClassDeclarationTranslate translate = new ClassDeclarationTranslate();

        return translate.translateClassDeclaration(parseTree);
    }

    private String formatCode(String newCoupleCode) throws Exception {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();
        String tmpFilepath = taskParameter.getOutputFilepath()+ UUID.randomUUID()+".cpp";

        FileWriteUtil.writeDataToFile(Lists.newArrayList(newCoupleCode),tmpFilepath);

        String cmd = "clang-format " + tmpFilepath;
        List<String> lines = CallUtil.callCMD(cmd);
        String formatNewCode = Joiner.on(" \n ").join(lines);
        new File(tmpFilepath).deleteOnExit();
        return formatNewCode;
    }

    public void fillTestMethodDiffCommon(ParserRuleContext parserRuleContext, TestMethodDiff testMethodDiff) {
        testMethodDiff.setBusinessKey(TestCodeContext.filepath + "$" + parserRuleContext.getStart().getLine());
        testMethodDiff.setStatus(TestMethodStatus.DEFAULT.getCode());
        testMethodDiff.setFilepath(TestCodeContext.filepath);
        fillTestMethodDiffOldCode(TestCodeContext.filepath, parserRuleContext, testMethodDiff);
    }

    public void fillTestMethodDiffClassMapping(TranslateCodeCollector.MethodTranslateCode methodTranslateCode, TestMethodDiff testMethodDiff) {
        ClassMappingReplace classMappingReplace = new ClassMappingReplace();
        String mappingTip = classMappingReplace.replaceClassMappingIfNeed(methodTranslateCode.methodHeaderTranslateCode);
        List<String> mappingTips = classMappingReplace.replaceClassMappingIfNeed(methodTranslateCode.blockStatementTranslateCodes);
        mappingTips.add(mappingTip);
        List<String> mappingTipList = mappingTips.stream()
                .distinct()
                .collect(Collectors.toList());
        String mappingTipStr = Joiner.on(" ").join(mappingTipList);
        testMethodDiff.setClassMappingTips(mappingTipStr);
    }

    public void fillTestMethodDiffClassMapping(TranslateCodeCollector.TranslateCode translateCode, TestMethodDiff testMethodDiff) {
        ClassMappingReplace classMappingReplace = new ClassMappingReplace();
        String mappingTip = classMappingReplace.replaceClassMappingIfNeed(translateCode);
        testMethodDiff.setClassMappingTips(mappingTip);
    }

    public void fillTestMethodDiffMethodName(ParserRuleContext ctx, TestMethodDiff testMethodDiff) {
        TestMethodBasicService testMethodBasicService = new TestMethodBasicService();
        String methodName = testMethodBasicService.fetchMethodName(ctx);
        testMethodDiff.setMethodName(methodName);
        // 设置type
        boolean testMethod = testMethodBasicService.isTestMethod((Java8Parser.MethodDeclarationContext) ctx);
        if (methodName.toLowerCase().startsWith("setup") || methodName.toLowerCase().startsWith("teardown")) {
            testMethodDiff.setType(Constants.CodeDiffType.SetupTearDown);
            return;
        }

        if (testMethod) {
            testMethodDiff.setType(Constants.CodeDiffType.TestMethod);
        } else {
            testMethodDiff.setType(Constants.CodeDiffType.OtherMethod);
        }
    }

    public void fillTestMethodNameAndType(ParserRuleContext ctx, String type, TestMethodDiff testMethodDiff) {
        testMethodDiff.setMethodName(ctx.getStart().getText());
        testMethodDiff.setType(type);
    }

    private void fillTestMethodDiffOldCode(String filepath, ParserRuleContext parserRuleContext, TestMethodDiff testMethodDiff) {
        int startLine = parserRuleContext.getStart().getLine();
        int endLine = parserRuleContext.getStop().getLine();
        List<String> srcCodeList = Lists.newArrayList();
        try {
            List<String> srcLines = FileReadUtil.readFile(filepath);
            srcCodeList = srcLines.subList(startLine - 1, endLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String oldCode = Joiner.on("\n").join(srcCodeList);
        testMethodDiff.setOldCode(oldCode);
    }

    /**
     * CRUD
     **/
    public List<TestMethodDiff> list() {
        List<TestMethodDiff> list = Lists.newArrayList();
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            TestMethodDiffDao mapper = session.getMapper(TestMethodDiffDao.class);
            list = mapper.selectAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void save(TestMethodDiff testMethodDiff) {
        if (testMethodDiff == null) {
            return;
        }

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            TestMethodDiffDao mapper = session.getMapper(TestMethodDiffDao.class);
            mapper.insert(testMethodDiff);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(TestMethodDiff testMethodDiff) {
        if (testMethodDiff == null) {
            return;
        }

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            TestMethodDiffDao mapper = session.getMapper(TestMethodDiffDao.class);
            mapper.update(testMethodDiff);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TestMethodDiff getByBusinessKey(String businessKey) {
        if (businessKey == null) {
            return null;
        }

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            TestMethodDiffDao mapper = session.getMapper(TestMethodDiffDao.class);
            return mapper.getByBusinessKey(businessKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
