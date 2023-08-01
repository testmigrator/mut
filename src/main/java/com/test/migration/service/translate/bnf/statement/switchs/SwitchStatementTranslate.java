package com.test.migration.service.translate.bnf.statement.switchs;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import com.test.migration.service.translate.bnf.statement.ExpressionStatementTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class SwitchStatementTranslate {

    /**
     * switchStatement
     * :	'switch' '(' expression ')' switchBlock
     * ;
     */
    public String translateSwitchStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_switchStatement) {
            Log.error("RULE_switchStatement error");
            return null;
        }
        ParserRuleContext expressionCtx = null;
        ParserRuleContext switchBlockCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_expression) {
                expressionCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_switchBlock) {
                switchBlockCtx = (ParserRuleContext) childRuleContext;
            }
        }

        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        SwitchBlockTranslate switchBlockTranslate = new SwitchBlockTranslate();
        String expression = expressionCtx == null ? "" : expressionTranslate.translateExpression(expressionCtx);
        String switchBlock = switchBlockCtx == null ? "" : switchBlockTranslate.translateSwitchBlock(switchBlockCtx);

        return "switch" + " (" + expression + ")" + switchBlock;
    }
}
