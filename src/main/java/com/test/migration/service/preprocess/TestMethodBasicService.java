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

            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
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
                ParseTree methodHeaderChild = node.getChild(j);
                boolean isMethodHeaderChildRuleContext = methodHeaderChild instanceof RuleContext;
                if (!isMethodHeaderChildRuleContext) {
                    continue;
                }
                RuleContext methodHeaderChildNode = (RuleContext) methodHeaderChild;

                if (methodHeaderChildNode.getRuleIndex() == Java8Parser.RULE_methodDeclarator) {
                    for (int k = 0; k < methodHeaderChildNode.getChildCount(); k++) {
                        ParseTree child1 = methodHeaderChildNode.getChild(k);
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

            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
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
                ParseTree methodHeaderChild = node.getChild(j);
                boolean isMethodHeaderChildRuleContext = methodHeaderChild instanceof RuleContext;
                if (!isMethodHeaderChildRuleContext) {
                    continue;
                }
                RuleContext methodHeaderChildNode = (RuleContext) methodHeaderChild;
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

            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
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
                ParseTree methodHeaderChild = node.getChild(j);
                boolean isMethodHeaderChildRuleContext = methodHeaderChild instanceof RuleContext;
                if (!isMethodHeaderChildRuleContext) {
                    continue;
                }
                RuleContext methodHeaderChildNode = (RuleContext) methodHeaderChild;

                if (methodHeaderChildNode.getRuleIndex() == Java8Parser.RULE_methodDeclarator) {
                    for (int k = 0; k < methodHeaderChildNode.getChildCount(); k++) {
                        ParseTree child1 = methodHeaderChildNode.getChild(k);

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
