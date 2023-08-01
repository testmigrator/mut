package com.test.migration.service.translate.bnf.common.constructor;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class ConstructorDeclarationTranslate {

    /**
     * constructorDeclaration
     * :	constructorModifier* constructorDeclarator throws_? constructorBody
     * ;
     */
    public String translateConstructorDeclaration(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_constructorDeclaration)) {
            Log.error("RULE_constructorDeclaration error");
            return null;
        }

        ParserRuleContext constructorDeclaratorCtx = null;
        ParserRuleContext constructorBodyCtx = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_constructorDeclarator) {
                    constructorDeclaratorCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_constructorBody) {
                    constructorBodyCtx = (ParserRuleContext) child;
                }
            }
        }

        ConstructorDeclaratorTranslate constructorDeclaratorTranslate = new ConstructorDeclaratorTranslate();
        ConstructorBodyTranslate constructorBodyTranslate = new ConstructorBodyTranslate();

        String constructorDeclarator = constructorDeclaratorCtx == null ? "" : constructorDeclaratorTranslate.translateConstructorDeclarator(constructorDeclaratorCtx);
        String constructorBody = constructorBodyCtx == null ? "" : constructorBodyTranslate.translateConstructorBody(constructorBodyCtx);

        return constructorDeclarator + " " + constructorBody;
    }
}
