package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class ReturnStatementTranslate {

    public String translateReturnStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_returnStatement) {
            Log.error("RULE_returnStatement error");
            return null;
        }
        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof RuleContext
                    && ((RuleContext) ctx.getChild(i)).getRuleIndex() == Java8Parser.RULE_expression) {
                stringBuilder.append(expressionTranslate.translateExpression((ParserRuleContext) ctx.getChild(i)));
            }
        }
        return "return " + stringBuilder + ";";
    }
}
