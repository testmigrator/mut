package com.test.migration.service.translate.bnf.common.cls;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class InterfaceTypeLfClassOrInterfaceTypeTranslate {

    /**
     * interfaceType_lf_classOrInterfaceType
     * 	:	classType_lf_classOrInterfaceType
     * 	;
     */
    public String translateInterfaceType_lf_classOrInterfaceType(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_interfaceType_lf_classOrInterfaceType)) {
            Log.error("RULE_interfaceType_lf_classOrInterfaceType error");
            return null;
        }
        ParserRuleContext child = (ParserRuleContext) ctx.getChild(0);
        ClassTypeLfClassOrInterfaceTypeTranslate translate = new ClassTypeLfClassOrInterfaceTypeTranslate();
        return translate.translateClassType_lf_classOrInterfaceType(child);
    }
}
