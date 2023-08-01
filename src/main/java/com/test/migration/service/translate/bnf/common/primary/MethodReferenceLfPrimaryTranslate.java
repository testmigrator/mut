package com.test.migration.service.translate.bnf.common.primary;

import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.TypeArgumentsTranslate;
import com.test.migration.service.translate.bnf.common.parameter.FormalParameterListTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.Log;

public class MethodReferenceLfPrimaryTranslate {

    /**
     * methodReference_lf_primary
     * :	'::' typeArguments? Identifier
     * ;
     */
    public String translateMethodReferenceLfPrimary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_methodReference_lf_primary) {
            Log.error("RULE_methodReference_lf_primary error");
            return null;
        }
        String identifier = "";
        String typeArguments = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode.getText();
                }
            }

            if (child instanceof RuleContext) {
                RuleContext ruleContext = (RuleContext) child;
                if (ruleContext.getRuleIndex() == Java8Parser.RULE_typeArguments) {
                    TypeArgumentsTranslate typeArgumentsTranslate = new TypeArgumentsTranslate();
                    typeArguments = typeArgumentsTranslate.translateTypeArguments((ParserRuleContext) ruleContext);
                }
            }
        }

        return "::" + typeArguments + " " + identifier;
    }
}
