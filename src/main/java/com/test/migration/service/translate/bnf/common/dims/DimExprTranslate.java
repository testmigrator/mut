package com.test.migration.service.translate.bnf.common.dims;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class DimExprTranslate {

    /**
     * dimExpr
     * :	annotation* '[' expression ']'
     * ;
     */
    public String translateDimExpr(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_dimExpr)) {
            Log.error("RULE_dimExpr error");
            return null;
        }
        ParserRuleContext expressionCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_expression) {
                    expressionCtx = (ParserRuleContext) child;
                }
            }
        }
        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        String expression = expressionCtx == null ? "" : expressionTranslate.translateExpression(expressionCtx);

        return "[" + expression + "]";
    }
}
