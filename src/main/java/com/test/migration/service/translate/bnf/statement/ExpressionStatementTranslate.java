package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.StatementExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class ExpressionStatementTranslate {

    /**
     * expressionStatement
     * 	:	statementExpression ';'
     * 	;
     * @param ctx
     * @return
     */
    public String translateExpressionStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_expressionStatement) {
            Log.error("RULE_expressionStatement error");
            return null;
        }
        StatementExpressionTranslate statementExpressionTranslate = new StatementExpressionTranslate();
        return statementExpressionTranslate.translateStatementExpression((ParserRuleContext) ctx.getChild(0)) + ";";
    }


}
