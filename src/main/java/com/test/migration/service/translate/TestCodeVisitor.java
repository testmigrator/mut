package com.test.migration.service.translate;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.test.migration.antlr.java.Java8BaseVisitor;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class TestCodeVisitor extends Java8BaseVisitor<RuleNode> {


    /**
     * typeName对应的类型mapping表，是实现代码转换中非常关键的一个环节
     * 在visitor过程中完成typename的初始化
     * <p>
     * 举个例子：
     * <p>
     * typeName.methodIdentifier:
     * mActivityRule.runOnUiThread(()->{
     * <p>
     * });
     * typeName是mActivityRule，对应的类型是ActivityTestRule<AnimatorSetActivity>，mapping规则中保存的是ActivityTestRule<AnimatorSetActivity>
     * 因此需要一个从mActivityRule到ActivityTestRule<AnimatorSetActivity>的映射关系
     * methodIdentifier是runOnUiThread
     * <p>
     * todo 单层映射（identifier重名问题）
     */

    @Override
    public RuleNode visitNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {
        // 一个testCode里面可能有N个嵌套类定义，这个if表示只收集顶级类
        if (StringUtils.isBlank(TestCodeContext.className)) {
            for (int i = 0; i < ctx.getChildCount(); i++) {
                ParseTree child = ctx.getChild(i);
                if (child instanceof TerminalNode) {
                    TerminalNode terminalNode = (TerminalNode) child;
                    if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                        TestCodeContext.className = terminalNode.getText();
                    }
                }
            }
        }
        return visitChildren(ctx);
    }

    /**
     * @param ctx the parse tree
     * @return
     */
    @Override
    public RuleNode visitClassBody(Java8Parser.ClassBodyContext ctx) {
        if (!TestCodeContext.isTopClass) {
            return visitChildren(ctx);
        }

        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }

            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_classBodyDeclaration) {
                fillClassMemberContext(childRuleContext);

                fillBasicContext(childRuleContext);
            }
        }
        TestCodeContext.isTopClass = false;
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        // 完成typename的设置, key为变量名，value为类型
        fillTypeNameMap(ctx);
        return visitChildren(ctx);
    }


    @Override
    public RuleNode visitFormalParameter(Java8Parser.FormalParameterContext ctx) {
        // 完成typename的设置, key为变量名，value为类型
        fillTypeNameMapFormalParameter(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {
        // 完成typename的设置, key为变量名，value为类型
        fillTypeNameMap(ctx);
        return visitChildren(ctx);
    }

    /*
        私有方法区
     */

    private void fillTypeNameMap(ParserRuleContext ctx) {
        List<String> typeNameKeys = findTypeNameKeys(ctx);
        String typeNameValue = findTypeNameValue(ctx);
        if (StringUtils.isNotBlank(typeNameValue)) {
            typeNameKeys.forEach(key -> {
                TestCodeContext.typeNameMap.put(key, typeNameValue);
            });
        }
    }

    private void fillTypeNameMapFormalParameter(ParserRuleContext ctx) {
        String key = "";
        String value = "";
        // AClass a,b,c=new xxx(); 作用是获取a,b,c为key， AClass为value
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_unannType) {
                    RuleContext child1 = (RuleContext) child.getChild(0);
                    if (child1.getRuleIndex() == Java8Parser.RULE_unannReferenceType) {
                        value = child1.getText();
                    }
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_variableDeclaratorId) {
                    if (child.getText().contains("=")) {
                        key = child.getText().split("=")[0].trim();
                    } else {
                        key = child.getText();
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            TestCodeContext.typeNameMap.put(key, value);
        }
    }


    private String findTypeNameValue(ParserRuleContext ctx) {
        //unannType
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_unannType) {
                    RuleContext child1 = (RuleContext) child.getChild(0);
                    if (child1.getRuleIndex() == Java8Parser.RULE_unannReferenceType) {
                        return child1.getText();
                    }
                }
            }
        }
        return null;
    }

    private List<String> findTypeNameKeys(ParserRuleContext ctx) {
        //variableDeclaratorList

        // AClass a,b,c=new xxx(); 作用是获取a,b,c为key， AClass为value
        List<String> typeNameKeys = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (!(child instanceof RuleContext)) {
                continue;
            }
            if (((RuleContext) child).getRuleIndex() != Java8Parser.RULE_variableDeclaratorList) {
                continue;
            }
            for (int j = 0; j < child.getChildCount(); j++) {
                ParseTree child1 = child.getChild(j);
                if (!(child1 instanceof RuleContext)) {
                    continue;
                }
                RuleContext childNode = (RuleContext) child1;
                if (childNode.getRuleIndex() != Java8Parser.RULE_variableDeclarator) {
                    continue;
                }
                if (childNode.getText().contains("=")) {
                    typeNameKeys.add(childNode.getText().split("=")[0].trim());
                } else {
                    typeNameKeys.add(childNode.getText());
                }
            }
        }
        return typeNameKeys;
    }

    /**
     * 填充方法名，类名，成员变量名等基础信息
     *
     * @param childRuleContext
     */
    private static void fillBasicContext(RuleContext childRuleContext) {
        if (childRuleContext.getChildCount() == 0) {
            return;
        }
        RuleContext childContext = (RuleContext) childRuleContext.getChild(0);
        if (childContext.getRuleIndex() == Java8Parser.RULE_classMemberDeclaration) {
            ParseTree classMemberChild = childContext.getChild(0);
            boolean isClassMemberRuleContext = classMemberChild instanceof RuleContext;
            if (!isClassMemberRuleContext) {
                return;
            }

            // 解析field
            if (((RuleContext) classMemberChild).getRuleIndex() == Java8Parser.RULE_fieldDeclaration) {
                fillFields(classMemberChild);
            }

            // 解析method todo
            if (((RuleContext) classMemberChild).getRuleIndex() == Java8Parser.RULE_methodDeclaration) {

            }

            // 解析class
            // TODO class可能有多层嵌套定义，目前只支持到二层
            if (((RuleContext) classMemberChild).getRuleIndex() == Java8Parser.RULE_classDeclaration) {
                fillMethods(classMemberChild);
            }

            // 解析interface todo
            if (((RuleContext) classMemberChild).getRuleIndex() == Java8Parser.RULE_interfaceDeclaration) {

            }
        }
    }

    /**
     * 填充成员变量子树，方法子树，类子树等信息
     */
    private static void fillClassMemberContext(RuleContext childRuleContext) {
        ParseTree child = childRuleContext.getChild(0);
        boolean isChildRuleContext = child instanceof RuleContext;
        if (!isChildRuleContext) {
            return;
        }
        RuleContext node = (RuleContext) child;
        if (node.getRuleIndex() != Java8Parser.RULE_classMemberDeclaration) {
            return;
        }

        ParseTree declarationChild = node.getChild(0);
        boolean isSubRuleContext = declarationChild instanceof RuleContext;
        if (!isSubRuleContext) {
            return;
        }
        RuleContext subNode = (RuleContext) declarationChild;
        if (subNode.getRuleIndex() == Java8Parser.RULE_fieldDeclaration) {
            TestCodeContext.fieldDeclarationCtxList.add((ParserRuleContext) subNode);
        }
        if (subNode.getRuleIndex() == Java8Parser.RULE_methodDeclaration) {
            TestCodeContext.methodDeclarationCtxList.add((ParserRuleContext) subNode);
        }
        if (subNode.getRuleIndex() == Java8Parser.RULE_classDeclaration) {
            TestCodeContext.classDeclarationCtxList.add((ParserRuleContext) subNode);
        }
    }

    private static void fillMethods(ParseTree classMemberChild) {
        ParseTree classMemberChildChild = classMemberChild.getChild(0);
        // normalClassDeclaration
        RuleContext classMemberChildRuleCtx = (RuleContext) classMemberChildChild;
        if (classMemberChildRuleCtx.getRuleIndex() == Java8Parser.RULE_normalClassDeclaration) {
            String identifier = "";
            for (int j = 0; j < classMemberChildRuleCtx.getChildCount(); j++) {
                ParseTree child1 = classMemberChildRuleCtx.getChild(j);
                if (child1 instanceof TerminalNode) {
                    TerminalNode terminalNode = (TerminalNode) child1;
                    if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                        identifier = terminalNode.getText();
                        break;
                    }
                }
            }
            TestCodeContext.ClassMemberDeclaration.Class cls = new TestCodeContext.ClassMemberDeclaration.Class();
            cls.name = identifier;
            TestCodeContext.ClassMemberDeclaration.classes.add(cls);
        }
    }

    /**
     * @param classMemberChild RULE_fieldDeclarations
     */
    private static void fillFields(ParseTree classMemberChild) {
        ParserRuleContext unannTypeRule = null;
        ParserRuleContext variableDeclaratorListRule = null;
        for (int j = 0; j < classMemberChild.getChildCount(); j++) {
            if (classMemberChild.getChild(j) instanceof RuleContext &&
                    ((RuleContext) classMemberChild.getChild(j)).getRuleIndex() == Java8Parser.RULE_unannType) {
                unannTypeRule = (ParserRuleContext) classMemberChild.getChild(j);
            }
            if (classMemberChild.getChild(j) instanceof RuleContext &&
                    ((RuleContext) classMemberChild.getChild(j)).getRuleIndex() == Java8Parser.RULE_variableDeclaratorList) {
                variableDeclaratorListRule = (ParserRuleContext) classMemberChild.getChild(j);
            }
        }
        String unannType = "";
        if (unannTypeRule != null) {
            unannType = unannTypeRule.getText();
        }
        List<String> variableDeclaratorList = Lists.newArrayList();
        if (variableDeclaratorListRule != null) {
            variableDeclaratorList = Splitter.on(",").trimResults().splitToList(variableDeclaratorListRule.getText());
        }
        String finalUnannType = unannType;
        TestCodeContext.ClassMemberDeclaration.fields.addAll(variableDeclaratorList.stream()
                .map(variableDeclarator -> {
                    TestCodeContext.ClassMemberDeclaration.Field field = new TestCodeContext.ClassMemberDeclaration.Field();
                    field.type = finalUnannType;
                    if (variableDeclarator.contains("=")) {
                        variableDeclarator = variableDeclarator.split("=")[0];
                    }
                    field.name = variableDeclarator;
                    return field;
                }).collect(Collectors.toList()));
    }
}
