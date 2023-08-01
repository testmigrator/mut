package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class LiteralTranslate {

    /**
     * literal
     * 	:	IntegerLiteral
     * 	|	FloatingPointLiteral
     * 	|	BooleanLiteral
     * 	|	CharacterLiteral
     * 	|	StringLiteral
     * 	|	NullLiteral
     * 	;
     * @param ctx
     * @return
     */
    public String translateLiteral(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_literal) {
            Log.error("RULE_literal error");
            return null;
        }

        return ctx.getText();
    }
}
