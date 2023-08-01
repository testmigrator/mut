package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.Log;

public class TypeParameterTranslate {

    /**
     * typeParameter
     * :	typeParameterModifier* Identifier typeBound?
     * ;
     */
    public String translateTypeParameter(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_typeParameter)) {
            Log.error("RULE_typeParameter error");
            return null;
        }

        String identifier = "";
        ParserRuleContext typeBoundCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode.getText();
                }
            }

            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_typeBound) {
                    typeBoundCtx = (ParserRuleContext) child;
                }
            }
        }

        TypeBoundTranslate typeBoundTranslate = new TypeBoundTranslate();
        String typeBound = typeBoundCtx == null ? "" : typeBoundTranslate.translateTypeBound(typeBoundCtx);

        return identifier + " " + typeBound;
    }
}
