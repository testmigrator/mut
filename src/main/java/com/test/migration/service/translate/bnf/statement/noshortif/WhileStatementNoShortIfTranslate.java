package com.test.migration.service.translate.bnf.statement.noshortif;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import com.test.migration.service.translate.bnf.statement.StatementTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class WhileStatementNoShortIfTranslate {

    /**
     * whileStatementNoShortIf
     * 	:	'while' '(' expression ')' statementNoShortIf
     * 	;
     */
    public String translateWhileStatementNoShortIf(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_whileStatementNoShortIf) {
            Log.error("RULE_whileStatementNoShortIf error");
            return null;
        }

        String expression = "";
        String statementNoShortIf = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_expression) {
                ExpressionTranslate expressionTranslate = new ExpressionTranslate();
                expression = expressionTranslate.translateExpression((ParserRuleContext) child);
            }
            if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_statement) {
                StatementNoShortIfTranslate statementTranslate = new StatementNoShortIfTranslate();
                statementNoShortIf = statementTranslate.translateStatementNoShortIf((ParserRuleContext) child);
            }
        }
        return "while" + " (" + expression + ")" + statementNoShortIf;
    }


}
