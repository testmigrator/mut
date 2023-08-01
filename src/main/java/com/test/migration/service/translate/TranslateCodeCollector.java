package com.test.migration.service.translate;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 收集转换后的代码结构，两个类别：
 * 1.testCode类成员变量：属性，内部类，非测试方法；其中每一个都带有对应的hint
 * <p>
 * <p>
 * 2.测试方法
 * 方法由blockStatement组成，测试方法用一组[blockStatement,hint]组成
 * <p>
 * 在translate的时候：1.收集转换后的结果 2.收集hint
 * 方便后续生成最终的迁移后代码
 * <p>
 * 每次执行translate之前，调用context的init方法初始化
 * translate过程中会不断填充成员变量，在translate执行完成后，完成填充
 */
@ToString
public class TranslateCodeCollector {

    public static String filepath;

    public static String className;
    public static List<TranslateCode> fieldDeclarationTranslateCodes;
    public static List<TranslateCode> classDeclarationTranslateCodes;
    public static List<MethodTranslateCode> methodDeclarationTranslateCodes;

    // 部分迁移 收集到的结果
    public static List<PartMigrationMethodTranslateCode> partMigrationMethodTranslateCodes;


    /***
     * TMP：辅助代码转换过程（中间结果）
     * 最终的代码合成部分不用以下这些中间结果
     */
    public static MethodTranslateCode.MethodHeaderTranslateCode methodHeaderTranslateCode;
    public static List<MethodTranslateCode.BlockStatementTranslateCode> blockStatementTranslateCodes;

    /**
     * low B设计：用这个标志来解完整转换和部分转换之间的冲突（乱用static导致：各种地方都在写入/读取相同static变量）
     * 根本解决方法在于舍弃static，老老实实new对象
     */
    public static boolean isFullTranslate;

    public static void init() {
        className = "";
        fieldDeclarationTranslateCodes = Lists.newArrayList();
        classDeclarationTranslateCodes = Lists.newArrayList();
        methodDeclarationTranslateCodes = Lists.newArrayList();

        // TODO tmp 慎用(lowB设计)
        methodHeaderTranslateCode = new MethodTranslateCode.MethodHeaderTranslateCode();
        blockStatementTranslateCodes = Lists.newArrayList();
        partMigrationMethodTranslateCodes = Lists.newArrayList();
        isFullTranslate = true;
    }

    public static class TranslateCode {
        public int startLine;
        public String translateCode;
        public List<String> misMatchCodes;
    }

    @ToString
    public static class MethodTranslateCode {
        public MethodHeaderTranslateCode methodHeaderTranslateCode;
        public List<BlockStatementTranslateCode> blockStatementTranslateCodes;
        // 对象
        public int startLine;

        /*** tmp ***/
        public static int methodStartLine;
        public static int methodEndLine;

        /**
         * method定义可能会出现多层嵌套
         */
        public static void clearMethod() {
            methodStartLine = 0;
            methodEndLine = 0;

//            TranslateCodeCollector.methodHeaderTranslateCode = new MethodTranslateCode.MethodHeaderTranslateCode();
            TranslateCodeCollector.blockStatementTranslateCodes = Lists.newArrayList();
        }

        @ToString
        public static class MethodHeaderTranslateCode {
            public String translateCode;
            public List<String> misMatchCodes;
        }

        @ToString
        @Getter
        public static class BlockStatementTranslateCode {
            public String translateCode;
            // 用于过滤嵌套bs收集
            public int tokenStartIndex;
            public int tokenStopIndex;
            public List<String> misMatchCodes;
        }
    }

    public static class PartMigrationMethodTranslateCode {
        public String translateCode;
    }
}
