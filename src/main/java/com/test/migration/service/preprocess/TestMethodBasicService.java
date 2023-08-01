package com.test.migration.service.preprocess;

import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.extract.APIExtractorVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

/**
 * 对test method的常用处理，比如：
 * 根据方法声明ctx[parseTree]来获取方法名，方法参数，方法返回值
 * 判断一个方法是否是test方法
 */
public class TestMethodBasicService {

    public String fetchMethodName(ParserRuleContext ctx){
        String methodName = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext node = (RuleContext) child;

            // 过滤掉非public的方法
            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                // 存在注解的方法
                boolean existAnnotation = isExistAnnotation(node);
                if (existAnnotation) {
                    continue;
                }

                if (!StringUtils.equalsAnyIgnoreCase(node.getText(), "public")) {
                    methodName = "";
                }
            }

            if (node.getRuleIndex() != Java8Parser.RULE_methodHeader) {
                continue;
            }

            for (int j = 0; j < node.getChildCount(); j++) {
                // 获取method的Header
                ParseTree methodHeaderChild = node.getChild(j);
                boolean isMethodHeaderChildRuleContext = methodHeaderChild instanceof RuleContext;
                if (!isMethodHeaderChildRuleContext) {
                    continue;
                }
                RuleContext methodHeaderChildNode = (RuleContext) methodHeaderChild;

                if (methodHeaderChildNode.getRuleIndex() == Java8Parser.RULE_methodDeclarator) {
                    for (int k = 0; k < methodHeaderChildNode.getChildCount(); k++) {
                        ParseTree child1 = methodHeaderChildNode.getChild(k);
                        // 填充methodName
                        if (child1 instanceof TerminalNode) {
                            TerminalNode terminalNode = (TerminalNode) child1;
                            if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                                methodName = terminalNode.getText();
                            }
                        }

                    }
                }
            }
        }
        return methodName;
    }

    public String fetchMethodReturn(ParserRuleContext ctx) {
        String methodReturn = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext node = (RuleContext) child;

            // 过滤掉非public的方法
            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                // 存在注解的方法
                boolean existAnnotation = isExistAnnotation(node);
                if (existAnnotation) {
                    continue;
                }

                if (!StringUtils.equalsAnyIgnoreCase(node.getText(), "public")) {
                    return "";
                }
            }

            if (node.getRuleIndex() != Java8Parser.RULE_methodHeader) {
                continue;
            }

            for (int j = 0; j < node.getChildCount(); j++) {
                // 获取method的Header
                ParseTree methodHeaderChild = node.getChild(j);
                boolean isMethodHeaderChildRuleContext = methodHeaderChild instanceof RuleContext;
                if (!isMethodHeaderChildRuleContext) {
                    continue;
                }
                RuleContext methodHeaderChildNode = (RuleContext) methodHeaderChild;
                // 填充methodReturn
                if (methodHeaderChildNode.getRuleIndex() == Java8Parser.RULE_result) {
                    methodReturn = methodHeaderChildNode.getText();
                }
            }
        }
        return methodReturn;
    }

    public String fetchMethodParameter(ParserRuleContext ctx) {
        String methodParameter = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext node = (RuleContext) child;

            // 过滤掉非public的方法
            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                // 存在注解的方法
                boolean existAnnotation = isExistAnnotation(node);
                if (existAnnotation) {
                    continue;
                }

                if (!StringUtils.equalsAnyIgnoreCase(node.getText(), "public")) {
                    return "";
                }
            }

            if (node.getRuleIndex() != Java8Parser.RULE_methodHeader) {
                continue;
            }

            for (int j = 0; j < node.getChildCount(); j++) {
                // 获取method的Header
                ParseTree methodHeaderChild = node.getChild(j);
                boolean isMethodHeaderChildRuleContext = methodHeaderChild instanceof RuleContext;
                if (!isMethodHeaderChildRuleContext) {
                    continue;
                }
                RuleContext methodHeaderChildNode = (RuleContext) methodHeaderChild;

                if (methodHeaderChildNode.getRuleIndex() == Java8Parser.RULE_methodDeclarator) {
                    for (int k = 0; k < methodHeaderChildNode.getChildCount(); k++) {
                        ParseTree child1 = methodHeaderChildNode.getChild(k);

                        // 填充methodParameter
                        if (child1 instanceof RuleNode) {
                            RuleNode ruleNode = (RuleNode) child1;
                            if (ruleNode.getRuleContext().getRuleIndex() == Java8Parser.RULE_formalParameterList) {
                                StringBuilder builder = new StringBuilder();
                                fetchCtxSourceText(ruleNode, builder);
                                methodParameter = builder.toString();
                            }
                        }
                    }
                }
            }
        }
        return methodParameter;
    }

    /**
     * 根据method的MethodDeclarationContext判断是否是测试方法
     *
     *
     * todo 更合理的方式，使用正则表达式：[A-Z][A-Za-z\d]*Test(s|Case)?|Test[A-Z][A-Za-z\d]*|IT(.*)|(.*)IT(Case)?'
     *
     * method name 以test为前后缀
     * method 注解 包含@Test
     *
     * @param ctx
     * @return
     */
    public boolean isTestMethod(Java8Parser.MethodDeclarationContext ctx){
        String methodName = fetchMethodName(ctx);
        if (methodName.toLowerCase().startsWith("test") || methodName.toLowerCase().endsWith("test")){
            return true;
        }

        return isAnnotationTest(ctx);
    }

    private boolean isExistAnnotation(RuleContext node) {
        boolean existAnnotation = false;
        for (int j = 0; j < node.getChildCount(); j++) {
            boolean isChildRuleContext = node.getChild(j) instanceof RuleContext;
            if (!isChildRuleContext) {
                continue;
            }
            RuleContext methodModifierNode = (RuleContext) node.getChild(j);
            if (methodModifierNode.getRuleIndex() == Java8Parser.RULE_annotation) {
                existAnnotation = true;
            }
        }
        return existAnnotation;
    }


    /**
     * 判断方法是否存在@Test注解
     *
     * @param ctx
     * @return
     */
    private boolean isAnnotationTest(Java8Parser.MethodDeclarationContext ctx) {
        boolean isAnnotationTest = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                RuleContext childRuleContext = (RuleContext) child;
                if (childRuleContext.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                    ParseTree methodModifierChildCtx = childRuleContext.getChild(0);
                    if (StringUtils.equals(methodModifierChildCtx.getText(), "@Test")) {
                        isAnnotationTest = true;
                        break;
                    }
                }
            }
        }
        return isAnnotationTest;
    }

    /**
     * 递归拼接node子节点的text信息，用空格分割
     */
    private void fetchCtxSourceText(ParseTree node, StringBuilder builder) {
        if (node.getChildCount() == 0) {
            builder.append(node.getText()).append(" ");
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            fetchCtxSourceText(node.getChild(i), builder);
        }
    }
}
