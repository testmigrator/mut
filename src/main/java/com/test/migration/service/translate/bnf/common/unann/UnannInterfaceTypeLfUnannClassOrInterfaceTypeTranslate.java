package com.test.migration.service.translate.bnf.common.unann;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class UnannInterfaceTypeLfUnannClassOrInterfaceTypeTranslate {

    /**
     * unannInterfaceType_lf_unannClassOrInterfaceType
     * 	:	unannClassType_lf_unannClassOrInterfaceType
     * 	;
     * @param ctx
     * @return
     */
    public String translateUnannInterfaceTypeLfUnannClassOrInterfaceType(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_unannInterfaceType_lf_unannClassOrInterfaceType) {
            Log.error("RULE_unannInterfaceType_lf_unannClassOrInterfaceType error");
            return null;
        }

        ParserRuleContext ctxChild = (ParserRuleContext) ctx.getChild(0);
        UnannClassTypeLfUnannClassOrInterfaceTypeTranslate translate = new UnannClassTypeLfUnannClassOrInterfaceTypeTranslate();

        return translate.translateUnannClassTypeLfUnannClassOrInterfaceType(ctxChild);
    }
}
