package com.test.migration.service.translate.bnf.common.parameter;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class LastFormalParameterTranslate {

    /**
     * lastFormalParameter
     * :	variableModifier* unannType annotation* '...' variableDeclaratorId
     * |	formalParameter
     * ;
     */
    public String translateLastFormalParameter(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_lastFormalParameter) {
            Log.error("RULE_lastFormalParameter error");
            return null;
        }
        if (ctx.getChildCount() != 1) {
            // todo 补充 hint  （AnimatorSet...sets）
            Log.error("todo 补充 hint  （AnimatorSet...sets）");
            return ctx.getText();
        }
        FormalParameterTranslate formalParameterTranslate = new FormalParameterTranslate();
        return formalParameterTranslate.translateFormalParameter((ParserRuleContext) ctx.getChild(0));
    }
}
