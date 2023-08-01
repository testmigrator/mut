package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class BreakStatementTranslate {

    /**
     * breakStatement
     * 	:	'break' Identifier? ';'
     * 	;
     * @param ctx
     * @return
     */
    public String translateBreakStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_breakStatement) {
            Log.error("RULE_breakStatement error");
            return null;
        }
        return ctx.getText();
    }
}
