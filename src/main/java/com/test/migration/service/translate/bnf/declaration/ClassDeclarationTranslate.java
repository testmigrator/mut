package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class ClassDeclarationTranslate {

    /***
     * classDeclaration
     * 	:	normalClassDeclaration
     * 	|	enumDeclaration
     * 	;
     *
     */
    public String translateClassDeclaration(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_classDeclaration)) {
            Log.error("RULE_classDeclaration error");
            return null;
        }

        ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(0);

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_normalClassDeclaration) {
            NormalClassDeclarationTranslate subTranslate = new NormalClassDeclarationTranslate();
            return subTranslate.translateNormalClassDeclaration(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_enumDeclaration) {
            EnumDeclarationTranslate subTranslate = new EnumDeclarationTranslate();
            return subTranslate.translateEnumDeclaration(childRuleContext);
        }

        Log.error("translateClassDeclaration error");

        return null;
    }
}
