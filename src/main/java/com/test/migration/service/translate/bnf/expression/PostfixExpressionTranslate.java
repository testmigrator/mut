package com.test.migration.service.translate.bnf.expression;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ExpressionNameTranslate;
import com.test.migration.service.translate.bnf.common.primary.PrimaryTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class PostfixExpressionTranslate {

    /**
     * postfixExpression
     * :	(	primary
     * |	expressionName
     * )
     * (	postIncrementExpression_lf_postfixExpression
     * |	postDecrementExpression_lf_postfixExpression
     * )*
     * ;
     */
    public String translatePostfixExpression(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_postfixExpression) {
            Log.error("postfixExpressionRule error");
            return null;
        }
        ParserRuleContext primaryCtx = null;
        ParserRuleContext expressionNameCtx = null;
        List<ParserRuleContext> postIncrementExpression_lf_postfixExpressionCtxList = Lists.newArrayList();
        List<ParserRuleContext> postDecrementExpression_lf_postfixExpressionList = Lists.newArrayList();

        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            ParserRuleContext ctxChild = (ParserRuleContext) ctx.getChild(i);
            if (ctxChild.getRuleIndex() == Java8Parser.RULE_primary) {
                primaryCtx = ctxChild;
            }
            if (ctxChild.getRuleIndex() == Java8Parser.RULE_expressionName) {
                expressionNameCtx = ctxChild;
            }
            if (ctxChild.getRuleIndex() == Java8Parser.RULE_postIncrementExpression_lf_postfixExpression) {
                postIncrementExpression_lf_postfixExpressionCtxList.add(ctxChild);
            }
            if (ctxChild.getRuleIndex() == Java8Parser.RULE_postDecrementExpression_lf_postfixExpression) {
                postDecrementExpression_lf_postfixExpressionList.add(ctxChild);
            }
        }

        PrimaryTranslate primaryTranslate = new PrimaryTranslate();
        ExpressionNameTranslate expressionNameTranslate = new ExpressionNameTranslate();

        String primary = primaryCtx == null ? "" : primaryTranslate.translatePrimary(primaryCtx);
        String expressionName = expressionNameCtx == null ? "" : expressionNameTranslate.translateExpressionName(expressionNameCtx);
        List<String> collect = postIncrementExpression_lf_postfixExpressionCtxList.stream()
                .map(this::translatePostIncrementExpression_lf_postfixExpression)
                .collect(Collectors.toList());
        String postIncrementExpression_lf_postfixExpression = Joiner.on(" ").join(collect);

        List<String> collect1 = postDecrementExpression_lf_postfixExpressionList.stream()
                .map(this::translatePostDecrementExpression_lf_postfixExpression)
                .collect(Collectors.toList());
        String postDecrementExpression_lf_postfixExpression = Joiner.on(" ").join(collect1);

        return primary + " "
                + expressionName + " "
                + postIncrementExpression_lf_postfixExpression + " "
                + postDecrementExpression_lf_postfixExpression;
    }

    /**
     * postIncrementExpression_lf_postfixExpression
     * :	'++'
     * ;
     *
     * @param ctx
     * @return
     */
    public String translatePostIncrementExpression_lf_postfixExpression(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_postIncrementExpression_lf_postfixExpression) {
            Log.error("RULE_postIncrementExpression_lf_postfixExpression error");
            return null;
        }
        return "++";
    }

    /**
     * postDecrementExpression_lf_postfixExpression
     * :	'--'
     * ;
     *
     * @param ctx
     * @return
     */
    public String translatePostDecrementExpression_lf_postfixExpression(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_postDecrementExpression_lf_postfixExpression) {
            Log.error("RULE_postDecrementExpression_lf_postfixExpression error");
            return null;
        }
        return "--";
    }


}
