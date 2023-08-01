package com.test.migration.service.translate.bnf.common.array;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.variable.VariableInitializerListTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

public class ArrayInitializerTranslate {

    /**
     * arrayInitializer
     * :	'{' variableInitializerList? ','? '}'
     * ;
     */
    public String translateArrayInitializer(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_arrayInitializer)) {
            Log.info("RULE_arrayInitializer error");
            return null;
        }

        String comma = "";
        ParserRuleContext variableInitializerListCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (StringUtils.equals(",", terminalNode.getText())) {
                    comma = ",";
                }
            }

            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_variableInitializerList) {
                    variableInitializerListCtx = (ParserRuleContext) child;
                }
            }
        }

        VariableInitializerListTranslate variableInitializerListTranslate = new VariableInitializerListTranslate();
        String variableInitializerList = variableInitializerListCtx == null ? "" : variableInitializerListTranslate.translateVariableInitializerList(variableInitializerListCtx);

        return " { " + variableInitializerList + comma + " } ";
    }


}
