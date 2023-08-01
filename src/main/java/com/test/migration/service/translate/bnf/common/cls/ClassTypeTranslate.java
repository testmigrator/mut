package com.test.migration.service.translate.bnf.common.cls;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class ClassTypeTranslate {
    
    /**
     * classType
     * 	:	annotation* Identifier typeArguments?
     * 	|	classOrInterfaceType '.' annotation* Identifier typeArguments?
     * 	;
     */
    public String translateClassType(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_classType)) {
            Log.info("RULE_classType error");
            return null;
        }
        Log.info("todo :translateClassType");
        return ctx.getText();
    }
}
