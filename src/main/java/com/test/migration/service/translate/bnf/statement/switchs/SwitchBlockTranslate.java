package com.test.migration.service.translate.bnf.statement.switchs;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class SwitchBlockTranslate {

    /**
     * switchBlock
     * :	'{' switchBlockStatementGroup* switchLabel* '}'
     * ;
     */
    public String translateSwitchBlock(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_switchBlock) {
            Log.error("RULE_switchBlock error");
            return null;
        }

        List<ParserRuleContext> switchBlockStatementGroupCtxList = Lists.newArrayList();
        List<ParserRuleContext> switchLabelCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_switchBlockStatementGroup) {
                switchBlockStatementGroupCtxList.add((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_switchLabel) {
                switchLabelCtxList.add((ParserRuleContext) childRuleContext);
            }
        }

        SwitchBlockStatementGroupTranslate switchBlockStatementGroupTranslate = new SwitchBlockStatementGroupTranslate();
        SwitchLabelTranslate switchLabelTranslate = new SwitchLabelTranslate();

        List<String> switchBlockStatementGroupStrList = switchBlockStatementGroupCtxList.stream()
                .map(switchBlockStatementGroupTranslate::translateSwitchBlockStatementGroup)
                .collect(Collectors.toList());
        String switchBlockStatementGroupStr = Joiner.on(" ").join(switchBlockStatementGroupStrList);

        List<String> switchLabelStrList = switchLabelCtxList.stream()
                .map(switchLabelTranslate::translateSwitchLabel)
                .collect(Collectors.toList());
        String switchLabelStr = Joiner.on(" ").join(switchLabelStrList);

        return " { " + switchBlockStatementGroupStr + " " + switchLabelStr + " } ";
    }
}
