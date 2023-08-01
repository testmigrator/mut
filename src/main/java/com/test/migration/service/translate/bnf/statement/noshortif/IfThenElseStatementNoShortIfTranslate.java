package com.test.migration.service.translate.bnf.statement.noshortif;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import com.test.migration.service.translate.bnf.statement.StatementTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;

public class IfThenElseStatementNoShortIfTranslate {

    /**
     * ifThenElseStatementNoShortIf
     * :	'if' '(' expression ')' statementNoShortIf 'else' statementNoShortIf
     * ;
     */
    public String translateIfThenElseNoShortIfStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_ifThenElseStatementNoShortIf) {
            Log.error("RULE_ifThenElseStatementNoShortIf error");
            return null;
        }

        ParserRuleContext expressionCtx = null;
        List<ParserRuleContext> statementNoShortIfCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_expression) {
                expressionCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_statementNoShortIf) {
                statementNoShortIfCtxList.add((ParserRuleContext) childRuleContext);
            }
        }

        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        String expression = expressionTranslate.translateExpression(expressionCtx);

        StatementNoShortIfTranslate statementTranslate = new StatementNoShortIfTranslate();
        String ifStatementNoShortIf = statementTranslate.translateStatementNoShortIf(statementNoShortIfCtxList.get(0));
        String elseStatementNoShortIf = statementTranslate.translateStatementNoShortIf(statementNoShortIfCtxList.get(1));

        return "if" + " (" + expression + ")" + ifStatementNoShortIf + "else" + elseStatementNoShortIf;
    }

}
