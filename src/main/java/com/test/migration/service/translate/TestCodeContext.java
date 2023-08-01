package com.test.migration.service.translate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.test.migration.antlr.java.Java8Parser;
import lombok.Data;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.Map;

/**
 * 代码转换上下文
 * <p>
 * 存储代码转换过程需要的中间信息
 * 简单实现：用static变量暂存antlr解析和生成的待转换code信息
 * 在antlr执行visitor完毕后，所有上下文信息将被填充，
 * 这些信息便于后续在其它地方使用，相当于一个简易内存数据库
 *
 * 用法问题：短时间内实现功能，设计略糙，更好的方式是用缓存
 */
public class TestCodeContext {

    public static String filepath;

    public static String className;

    /**
     * testCode的成员变量, 待迁移
     */
    public static List<ParserRuleContext> fieldDeclarationCtxList;
    public static List<ParserRuleContext> methodDeclarationCtxList;
    public static List<ParserRuleContext> classDeclarationCtxList;
    public static Map<String, String> typeNameMap;

    /***
     * 每个测试类内部的classBody不止一个，只对最外层class收集类成员变量
     *
     * 首次visit classBody之后，该值设为false
     */
    public static boolean isTopClass = true;

    /**
     * visit之前调用 清空历史数据
     */
    public static void init() {
        isTopClass = true;
        className = "";
        filepath = "";
        fieldDeclarationCtxList = Lists.newArrayList();
        methodDeclarationCtxList = Lists.newArrayList();
        classDeclarationCtxList = Lists.newArrayList();
        typeNameMap = Maps.newHashMap();
    }

    /**
     * visit之后调用 过滤不需要迁移的测试代码
     */
//    public static void filter() {
//        TestCodeFilter.filterFieldDeclarationCtxList();
//        TestCodeFilter.filterMethodDeclarationCtxList();
//        TestCodeFilter.filterClassDeclarationCtxList();
//    }

    /**
     * 保存test code里面定义的类属性基础信息（name，type）：变量，常量等
     */

    public static class ClassMemberDeclaration {
        // field,method,class,interface
        public static List<Field> fields = Lists.newArrayList();
        public static List<Method> methods = Lists.newArrayList();
        public static List<Class> classes = Lists.newArrayList();
        public static List<Interface> interfaces = Lists.newArrayList();

        @Data
        public static class Field {
            public String type;
            public String name;
        }

        @Data
        public static class Method {
            public static String name;
        }

        @Data
        public static class Class {
            public String name;
        }

        public static class Interface {
            public static String name;
        }
    }
}
