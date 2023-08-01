package com.test.migration.service.translate.bnf.common.constructor;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.parameter.FormalParameterListTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class ConstructorDeclaratorTranslate {

    /**
     * constructorDeclarator
     * :	typeParameters? simpleTypeName '(' formalParameterList? ')'
     * ;
     */
    public String translateConstructorDeclarator(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_constructorDeclarator)) {
            Log.error("RULE_constructorDeclarator error");
            return null;
        }

        ParserRuleContext simpleTypeNameCtx = null;
        ParserRuleContext formalParameterListCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_simpleTypeName) {
                    simpleTypeNameCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_formalParameterList) {
                    formalParameterListCtx = (ParserRuleContext) child;
                }
            }
        }
        SimpleTypeNameTranslate simpleTypeNameTranslate = new SimpleTypeNameTranslate();
        FormalParameterListTranslate formalParameterListTranslate = new FormalParameterListTranslate();

        String simpleTypeName = simpleTypeNameCtx == null ? "" : simpleTypeNameTranslate.translateSimpleTypeName(simpleTypeNameCtx);
        String formalParameterList = formalParameterListCtx == null ? "" : formalParameterListTranslate.translateFormalParameterList(formalParameterListCtx);

        return simpleTypeName + " (" + formalParameterList + ") ";
    }
}
