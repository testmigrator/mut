package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class NormalInterfaceDeclarationTranslate {

    /**
     * normalInterfaceDeclaration
     * 	:	interfaceModifier* 'interface' Identifier typeParameters? extendsInterfaces? interfaceBody
     * 	;
     */
    public String translateNormalInterfaceDeclaration(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_normalInterfaceDeclaration)) {
            Log.error("RULE_normalInterfaceDeclaration error");
            return null;
        }

        Log.info("todo：interface代码转换 ");

        return ctx.getText();
    }
}
