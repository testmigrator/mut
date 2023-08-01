package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.unann.UnannTypeTranslate;
import com.test.migration.service.translate.bnf.common.variable.VariableDeclaratorListTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class FieldDeclarationTranslate {

    /**
     * fieldDeclaration
     * :	fieldModifier* unannType variableDeclaratorList ';'
     * ;
     */
    public String translateFieldDeclaration(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_fieldDeclaration)) {
            Log.error("RULE_fieldDeclaration error");
            return null;
        }
        // find unanntype variableDeclaratorList
        ParserRuleContext unannTypeCtx = null;
        ParserRuleContext variableDeclaratorListCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_unannType) {
                unannTypeCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_variableDeclaratorList) {
                variableDeclaratorListCtx = (ParserRuleContext) childRuleContext;
            }
        }

        UnannTypeTranslate unannTypeTranslate = new UnannTypeTranslate();
        String unannType = unannTypeTranslate.translateUnannType(unannTypeCtx);

        VariableDeclaratorListTranslate variableDeclaratorListTranslate = new VariableDeclaratorListTranslate();
        String variableDeclaratorList = variableDeclaratorListTranslate.translateVariableDeclaratorList(variableDeclaratorListCtx);

        return unannType + " " + variableDeclaratorList + ";";
    }
}
