package com.test.migration.service.translate.bnf.expression.lambda;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import com.test.migration.service.translate.bnf.statement.BlockTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class LambdaBodyTranslate {

    /**
     * lambdaBody
     * :	expression
     * |	block
     * ;
     */
    public String translateLambdaBody(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_lambdaBody) {
            Log.error("RULE_lambdaBody error");
            return null;
        }

        ParserRuleContext child = (ParserRuleContext) ctx.getChild(0);
        if (child.getRuleIndex() == Java8Parser.RULE_expression) {
            ExpressionTranslate expressionTranslate = new ExpressionTranslate();
            return expressionTranslate.translateExpression(child);
        }

        if (child.getRuleIndex() == Java8Parser.RULE_block) {
            BlockTranslate blockTranslate = new BlockTranslate();
            return blockTranslate.translateBlock(child);
        }

        Log.error("translateLambdaBody error");
        return null;
    }

}
