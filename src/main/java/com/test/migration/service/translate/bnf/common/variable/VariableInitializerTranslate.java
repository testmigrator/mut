package com.test.migration.service.translate.bnf.common.variable;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.array.ArrayInitializerTranslate;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class VariableInitializerTranslate {

    /**
     * variableInitializer
     * :	expression
     * |	arrayInitializer
     * ;
     */
    public String translateVariableInitializer(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_variableInitializer) {
            Log.error("RULE_variableInitializer error");
            return null;
        }
        ParserRuleContext childRule = (ParserRuleContext) ctx.getChild(0);

        if (childRule.getRuleIndex() == Java8Parser.RULE_arrayInitializer) {
            ArrayInitializerTranslate translate = new ArrayInitializerTranslate();
            return translate.translateArrayInitializer(childRule);
        }

        if (childRule.getRuleIndex() == Java8Parser.RULE_expression) {
            ExpressionTranslate translate = new ExpressionTranslate();
            return translate.translateExpression(childRule);
        }

        return null;
    }
}
