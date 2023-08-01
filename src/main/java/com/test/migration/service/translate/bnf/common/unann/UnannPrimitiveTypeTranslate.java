package com.test.migration.service.translate.bnf.common.unann;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class UnannPrimitiveTypeTranslate {

    /**
     * unannPrimitiveType
     * 	:	numericType
     * 	|	'boolean'
     * 	;
     */
    public String translateUnannPrimitiveType(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_unannPrimitiveType) {
            Log.error("RULE_unannPrimitiveType");
            return null;
        }

        return ctx.getText();
    }

}
