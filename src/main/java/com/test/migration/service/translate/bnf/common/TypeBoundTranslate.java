package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class TypeBoundTranslate {

    /**
     * typeBound
     * :	'extends' typeVariable
     * |	'extends' classOrInterfaceType additionalBound*
     * ;
     */
    public String translateTypeBound(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_typeBound)) {
            Log.error("RULE_typeBound error");
            return null;
        }

        Log.info("todo translateTypBound");

        return ctx.getText();
    }
}
