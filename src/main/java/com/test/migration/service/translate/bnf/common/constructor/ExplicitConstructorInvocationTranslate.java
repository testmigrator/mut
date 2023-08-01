package com.test.migration.service.translate.bnf.common.constructor;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ArgumentListTranslate;
import com.test.migration.service.translate.bnf.common.ExpressionNameTranslate;
import com.test.migration.service.translate.bnf.common.TypeArgumentsTranslate;
import com.test.migration.service.translate.bnf.common.primary.PrimaryTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class ExplicitConstructorInvocationTranslate {

    /**
     * explicitConstructorInvocation
     * :	typeArguments? 'this' '(' argumentList? ')' ';'
     * |	typeArguments? 'super' '(' argumentList? ')' ';'
     * |	expressionName '.' typeArguments? 'super' '(' argumentList? ')' ';'
     * |	primary '.' typeArguments? 'super' '(' argumentList? ')' ';'
     * ;
     */
    public String translateExplicitConstructorInvocation(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_explicitConstructorInvocation)) {
            Log.error("RULE_explicitConstructorInvocation error");
            return null;
        }

        ParserRuleContext typeArgumentsCtx = null;
        ParserRuleContext argumentListCtx = null;
        ParserRuleContext expressionNameCtx = null;
        ParserRuleContext primaryCtx = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_typeArguments) {
                    typeArgumentsCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_argumentList) {
                    argumentListCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_expressionName) {
                    expressionNameCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_primary) {
                    primaryCtx = (ParserRuleContext) child;
                }
            }
        }

        ArgumentListTranslate argumentListTranslate = new ArgumentListTranslate();
        TypeArgumentsTranslate typeArgumentsTranslate = new TypeArgumentsTranslate();
        ExpressionNameTranslate expressionNameTranslate = new ExpressionNameTranslate();
        PrimaryTranslate primaryTranslate = new PrimaryTranslate();

        String argumentList = argumentListCtx == null ? "" : argumentListTranslate.translateArgumentList(argumentListCtx);
        String typeArguments = typeArgumentsCtx == null ? "" : typeArgumentsTranslate.translateTypeArguments(typeArgumentsCtx);
        String expressionName = expressionNameCtx == null ? "" : expressionNameTranslate.translateExpressionName(expressionNameCtx);
        String primary = primaryCtx == null ? "" : primaryTranslate.translatePrimary(primaryCtx);
        ParseTree firstChild = ctx.getChild(0);
        boolean isRuleContext = firstChild instanceof RuleContext;
        if (!isRuleContext) {
            //typeArguments? 'super' '(' argumentList? ')' ';'
            if (firstChild.getText().contains("super")) {
                return typeArguments + " super " + " (" + argumentList + ") " + ";";
            }
            //typeArguments? 'this' '(' argumentList? ')' ';'
            return typeArguments + " this " + " (" + argumentList + ") " + ";";
        }

        int ruleIndex = ((RuleContext) firstChild).getRuleIndex();
        if (ruleIndex == Java8Parser.RULE_typeArguments) {
            //typeArguments? 'super' '(' argumentList? ')' ';'
            if (ctx.getText().contains("super")) {
                return typeArguments + " super " + " (" + argumentList + ") " + ";";
            }

            //typeArguments? 'this' '(' argumentList? ')' ';'
            return typeArguments + " this " + " (" + argumentList + ") " + ";";
        }

        //expressionName '.' typeArguments? 'super' '(' argumentList? ')' ';'
        if (ruleIndex == Java8Parser.RULE_expressionName) {
            return expressionName + "." + typeArguments + " super " + " (" + argumentList + ") " + ";";
        }

        //primary '.' typeArguments? 'super' '(' argumentList? ')' ';'
        if (ruleIndex == Java8Parser.RULE_primary) {
            return primary + "." + typeArguments + " super " + " (" + argumentList + ") " + ";";
        }

        Log.error("translateExplicitConstructorInvocation error");

        return ctx.getText();
    }
}
