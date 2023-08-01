package com.test.migration.service.translate.bnf.common.unann;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class UnannTypeVariableTranslate {
    /**
     * unannTypeVariable
     * 	:	Identifier
     * 	;
     */
    public String translateUnannTypeVariable(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_unannTypeVariable) {
            Log.error("RULE_unannTypeVariable error");
            return null;
        }

        return ctx.getText();
    }


}
