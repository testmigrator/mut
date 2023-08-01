package com.test.migration.service.translate.bnf.expression.lambda;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class LambdaExpressionTranslate {
    /**
     * lambdaExpression
     * :	lambdaParameters '->' lambdaBody
     * ;
     */
    public String translateLambdaExpression(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_lambdaExpression) {
            Log.error("RULE_lambdaExpression error");
            return null;
        }

        ParserRuleContext lambdaParametersCtx = null;
        ParserRuleContext lambdaBodyCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_lambdaParameters) {
                lambdaParametersCtx = childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_lambdaBody) {
                lambdaBodyCtx = childRuleContext;
            }
        }

        LambdaParametersTranslate lambdaParametersTranslate = new LambdaParametersTranslate();
        String lambdaParameters = lambdaParametersTranslate.translateLambdaParameters(lambdaParametersCtx);

        LambdaBodyTranslate lambdaBodyTranslate = new LambdaBodyTranslate();
        String lambdaBody = lambdaBodyTranslate.translateLambdaBody(lambdaBodyCtx);

        return lambdaParameters + "->" + lambdaBody;
    }

}
