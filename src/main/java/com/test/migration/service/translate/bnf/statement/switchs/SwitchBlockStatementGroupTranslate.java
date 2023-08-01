package com.test.migration.service.translate.bnf.statement.switchs;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import com.test.migration.service.translate.bnf.statement.BlockStatementsTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class SwitchBlockStatementGroupTranslate {

    /**
     * switchBlockStatementGroup
     * :	switchLabels blockStatements
     * ;
     */
    public String translateSwitchBlockStatementGroup(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_switchBlockStatementGroup) {
            Log.error("RULE_switchBlockStatementGroup error");
            return null;
        }

        ParserRuleContext switchLabelsCtx = null;
        ParserRuleContext blockStatementsCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_switchLabels) {
                switchLabelsCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_blockStatements) {
                blockStatementsCtx = (ParserRuleContext) childRuleContext;
            }
        }

        SwitchLabelsTranslate switchLabelsTranslate = new SwitchLabelsTranslate();
        BlockStatementsTranslate blockStatementsTranslate = new BlockStatementsTranslate();
        String switchLabels = switchLabelsCtx == null ? "" : switchLabelsTranslate.translateSwitchLabels(switchLabelsCtx);
        String blockStatements = blockStatementsCtx == null ? "" : blockStatementsTranslate.translateBlockStatements(blockStatementsCtx);

        return switchLabels + " " + blockStatements;
    }
}
