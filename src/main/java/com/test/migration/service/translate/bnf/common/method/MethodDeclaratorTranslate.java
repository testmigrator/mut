package com.test.migration.service.translate.bnf.common.method;

import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.parameter.FormalParameterListTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.Log;

public class MethodDeclaratorTranslate {

    /**
     * methodDeclarator
     * :	Identifier '(' formalParameterList? ')' dims?
     * ;
     */
    public String translateMethodDeclarator(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_methodDeclarator) {
            Log.error("RULE_methodDeclarator error");
            return null;
        }
        String identifier = "";
        String formalParameterList = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode)child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode.getText();
                }
            }

            if (child instanceof RuleContext) {
                RuleContext ruleContext = (RuleContext) child;
                if (ruleContext.getRuleIndex() == Java8Parser.RULE_formalParameterList) {
                    FormalParameterListTranslate formalParameterListTranslate = new FormalParameterListTranslate();
                    formalParameterList = formalParameterListTranslate.translateFormalParameterList((ParserRuleContext) ruleContext);
                }
            }
        }
        return identifier + " (" + formalParameterList + ")";
    }
}
