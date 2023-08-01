package com.test.migration.service.translate.bnf.statement.switchs;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class SwitchLabelsTranslate {

    /**
     * switchLabels
     * 	:	switchLabel switchLabel*
     * 	;
     */
    public String translateSwitchLabels(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_switchLabels) {
            Log.error("RULE_switchLabels error");
            return null;
        }

        List<ParserRuleContext> switchLabelCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_switchLabel) {
                switchLabelCtxList.add((ParserRuleContext) childRuleContext);
            }
        }

        SwitchLabelTranslate switchLabelTranslate = new SwitchLabelTranslate();
        List<String> switchLabelStrList = switchLabelCtxList.stream()
                .map(switchLabelTranslate::translateSwitchLabel)
                .collect(Collectors.toList());
        String switchLabelStr = Joiner.on(" ").join(switchLabelStrList);

        return switchLabelStr;
    }
}
