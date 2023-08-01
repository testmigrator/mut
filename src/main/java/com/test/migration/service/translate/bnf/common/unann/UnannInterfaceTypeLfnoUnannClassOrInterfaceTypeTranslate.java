package com.test.migration.service.translate.bnf.common.unann;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class UnannInterfaceTypeLfnoUnannClassOrInterfaceTypeTranslate {

    /**
     * unannInterfaceType_lfno_unannClassOrInterfaceType
     * 	:	unannClassType_lfno_unannClassOrInterfaceType
     * 	;
     * @param ctx
     * @return
     */
    public String translateUnannInterfaceTypeLfnoUnannClassOrInterfaceType(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_unannInterfaceType_lfno_unannClassOrInterfaceType) {
            Log.error("RULE_unannInterfaceType_lfno_unannClassOrInterfaceType error");
            return null;
        }
        ParserRuleContext ctxChild = (ParserRuleContext) ctx.getChild(0);
        UnannClassTypeLfnoUnannClassOrInterfaceTypeTranslate translate = new UnannClassTypeLfnoUnannClassOrInterfaceTypeTranslate();

        return translate.translateUnannClassTypeLfnoUnannClassOrInterfaceType(ctxChild);
    }
}
