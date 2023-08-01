package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class ThrowStatementTranslate {

    /**
     * throwStatement
     * :	'throw' expression ';'
     * ;
     */
    public String translateThrowStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_throwStatement) {
            Log.error("RULE_throwStatement error");
            return null;
        }

        ParserRuleContext expressionCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_expression) {
                expressionCtx = (ParserRuleContext) childRuleContext;
            }
        }
        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        String expression = expressionTranslate.translateExpression(expressionCtx);


        return "throw " + expression + ";";

    }

}
