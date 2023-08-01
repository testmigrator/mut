package com.test.migration.service.translate.bnf.expression.lambda;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.parameter.FormalParameterListTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class LambdaParametersTranslate {
    /**
     * lambdaParameters
     * :	Identifier
     * |	'(' formalParameterList? ')'
     * |	'(' inferredFormalParameterList ')'
     * ;
     */
    public String translateLambdaParameters(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_lambdaParameters) {
            Log.error("RULE_lambdaParameters error");
            return null;
        }
        //Identifier
        if (ctx.getChildCount() == 1) {
            return ctx.getText();
        }

        ParserRuleContext formalParameterListCtx = null;
        ParserRuleContext inferredFormalParameterListCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_formalParameterList) {
                formalParameterListCtx = childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_inferredFormalParameterList) {
                inferredFormalParameterListCtx = childRuleContext;
            }
        }
        // '(' inferredFormalParameterList ')'
        if (inferredFormalParameterListCtx != null) {
            InferredFormalParameterListTranslate inferredFormalParameterListTranslate = new InferredFormalParameterListTranslate();
            return " (" + inferredFormalParameterListTranslate.translateInferredFormalParameterList(inferredFormalParameterListCtx) + ")";
        } else {
            //'(' inferredFormalParameterList ')'
            FormalParameterListTranslate formalParameterListTranslate = new FormalParameterListTranslate();
            String formalParameterList = formalParameterListCtx == null ? "" : formalParameterListTranslate.translateFormalParameterList(formalParameterListCtx);
            return " (" + formalParameterList + ")";
        }
    }

}
