package com.test.migration.service.translate.bnf.common.method;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ArgumentListTranslate;
import com.test.migration.service.translate.bnf.common.ExpressionNameTranslate;
import com.test.migration.service.translate.bnf.common.TypeArgumentsTranslate;
import com.test.migration.service.translate.bnf.common.primary.PrimaryTranslate;
import com.test.migration.service.translate.replace.MethodInvocationReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;

public class MethodInvocationTranslate {

    /**
     * methodInvocation
     * :	methodName '(' argumentList? ')'
     * |	typeName '.' typeArguments? Identifier '(' argumentList? ')'
     * |	expressionName '.' typeArguments? Identifier '(' argumentList? ')'
     * |	primary '.' typeArguments? Identifier '(' argumentList? ')'
     * |	'super' '.' typeArguments? Identifier '(' argumentList? ')'
     * |	typeName '.' 'super' '.' typeArguments? Identifier '(' argumentList? ')'
     * ;
     */
    public String translateMethodInvocation(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_methodInvocation) {
            Log.error("RULE_methodInvocation error");
            return null;
        }
        // 1.获取各个类型子节点
        ParseTree identifier = null;
        ParserRuleContext methodNameCtx = null;
        ParserRuleContext argumentListCtx = null;
        ParserRuleContext typeArgumentsCtx = null;
        ParserRuleContext typeNameCtx = null;
        ParserRuleContext expressionNameCtx = null;
        ParserRuleContext primaryCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_methodName) {
                    methodNameCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_argumentList) {
                    argumentListCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_typeArguments) {
                    typeArgumentsCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_typeName) {
                    typeNameCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_expressionName) {
                    expressionNameCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_primary) {
                    primaryCtx = (ParserRuleContext) child;
                }
            }
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode;
                }
            }
        }
        ArgumentListTranslate argumentListTranslate = new ArgumentListTranslate();
        TypeArgumentsTranslate typeArgumentsTranslate = new TypeArgumentsTranslate();
        ExpressionNameTranslate expressionNameTranslate = new ExpressionNameTranslate();
        PrimaryTranslate primaryTranslate = new PrimaryTranslate();
        TypeNameTranslate typeNameTranslate = new TypeNameTranslate();
        MethodNameTranslate methodNameTranslate = new MethodNameTranslate();

        String identifierStr = identifier == null ? "" : identifier.getText();
        String methodName = methodNameCtx == null ? "" : methodNameTranslate.translateMethodName(methodNameCtx);
        String argumentList = argumentListCtx == null ? "" : argumentListTranslate.translateArgumentList(argumentListCtx);
        String typeArguments = typeArgumentsCtx == null ? "" : typeArgumentsTranslate.translateTypeArguments(typeArgumentsCtx);
        String typeName = typeNameCtx == null ? "" : typeNameTranslate.translateTypeName(typeNameCtx);
        String expressionName = expressionNameCtx == null ? "" : expressionNameTranslate.translateExpressionName(expressionNameCtx);
        String primary = primaryCtx == null ? "" : primaryTranslate.translatePrimary(primaryCtx);

        // 2. 判断第一个孩子节点的类型
        MethodInvocationReplace replace = new MethodInvocationReplace();
        ParseTree firstChild = ctx.getChild(0);
        boolean isRuleContext = firstChild instanceof RuleContext;
        if (!isRuleContext) {
            //'super' '.' typeArguments? Identifier '(' argumentList? ')'
            List<String> originals = Lists.newArrayList("super" , "." , typeArguments , identifierStr , " (" , argumentList , ")");
            return replace.replaceStructure5(originals);
        } else {
            int ruleIndex = ((RuleContext) firstChild).getRuleIndex();
            if (ruleIndex == Java8Parser.RULE_methodName) {
                //methodName '(' argumentList? ')'
                List<String> originals = Lists.newArrayList(methodName , " (" , argumentList , ")");
                return replace.replaceStructure1(originals);
            }

            if (ruleIndex == Java8Parser.RULE_expressionName) {
                //expressionName '.' typeArguments? Identifier '(' argumentList? ')'
                List<String> originals = Lists.newArrayList(expressionName , "." , typeArguments , identifierStr , " (" , argumentList , ")");
                return replace.replaceStructure3(originals);
            }

            if (ruleIndex == Java8Parser.RULE_primary) {
                //primary '.' typeArguments? Identifier '(' argumentList? ')'
                List<String> originals = Lists.newArrayList(primary , "." , typeArguments , identifierStr , " (" , argumentList , ")");
                return replace.replaceStructure4(originals);
            }

            if (ruleIndex == Java8Parser.RULE_typeName) {
                // 判断ctx中是否包含super
                boolean haveSuper = false;
                for (int i = 0; i < ctx.getChildCount(); i++) {
                    if (StringUtils.equals("super", ctx.getChild(i).getText())) {
                        haveSuper = true;
                    }
                }

                //typeName '.' 'super' '.' typeArguments? Identifier '(' argumentList? ')'
                if (haveSuper) {
                    List<String> originals = Lists.newArrayList(typeName, ".", "super", ".", typeArguments, identifierStr, " (", argumentList, ")");
                    return replace.replaceStructure6(originals);
                }

                //typeName '.' typeArguments? Identifier '(' argumentList? ')'
                else {
                    List<String> originals = Lists.newArrayList(typeName , "." , typeArguments , identifierStr , " (" , argumentList , ")");
                    return replace.replaceStructure2(originals);
                }
            }
        }

        Log.error("translateMethodInvocation error");
        return null;
    }
}
