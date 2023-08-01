package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class InterfaceDeclarationTranslate {

    /**
     * interfaceDeclaration
     * :	normalInterfaceDeclaration
     * |	annotationTypeDeclaration
     * ;
     */
    public String translateInterfaceDeclaration(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_interfaceDeclaration)) {
            Log.error("RULE_interfaceDeclaration error");
            return null;
        }
        ParserRuleContext ctxChild = (ParserRuleContext) ctx.getChild(0);

        if (ctxChild.getRuleIndex() == Java8Parser.RULE_normalInterfaceDeclaration) {
            NormalInterfaceDeclarationTranslate translate = new NormalInterfaceDeclarationTranslate();
            return translate.translateNormalInterfaceDeclaration(ctxChild);
        }

        if (ctxChild.getRuleIndex() == Java8Parser.RULE_annotationTypeDeclaration) {
            return ctx.getText();
        }

        return null;
    }
}
