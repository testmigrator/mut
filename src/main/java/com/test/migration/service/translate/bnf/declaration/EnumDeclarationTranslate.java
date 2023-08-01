package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class EnumDeclarationTranslate {
    /**
     * enumDeclaration
     * 	:	classModifier* 'enums' Identifier superinterfaces? enumBody
     * 	;
     */
    public String translateEnumDeclaration(RuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_enumDeclaration)) {
            Log.error("RULE_enumDeclaration error");
            return null;
        }

        Log.info("todo：enum代码转换 ");

        return ctx.getText();
    }
}
