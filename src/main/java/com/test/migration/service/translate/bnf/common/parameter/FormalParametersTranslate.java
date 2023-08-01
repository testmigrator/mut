package com.test.migration.service.translate.bnf.common.parameter;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;

public class FormalParametersTranslate {

    /**
     * formalParameters
     * :	formalParameter (',' formalParameter)*
     * |	receiverParameter (',' formalParameter)*
     * ;
     * <p>
     * 无视receiverParameter
     */
    public String translateFormalParameters(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_formalParameters) {
            Log.error("RULE_formalParameters error");
            return null;
        }
        List<ParserRuleContext> formalParameterList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_formalParameter) {
                formalParameterList.add((ParserRuleContext) childRuleContext);
            }
        }
        StringBuilder translateResult = new StringBuilder();
        FormalParameterTranslate formalParameterTranslate = new FormalParameterTranslate();
        for (int i = 0; i < formalParameterList.size(); i++) {
            if (i == formalParameterList.size() - 1) {
                translateResult.append((formalParameterTranslate.translateFormalParameter(formalParameterList.get(i))));
            } else {
                translateResult.append((formalParameterTranslate.translateFormalParameter(formalParameterList.get(i)))).append(",");
            }
        }
        return translateResult.toString();
    }
}
