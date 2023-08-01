package com.test.migration.service.translate.bnf.common.cls;

import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.TypeArgumentsTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.Log;

public class ClassTypeLfnoClassOrInterfaceTypeTranslate {

    /**
     * classType_lfno_classOrInterfaceType
     * :	annotation* Identifier typeArguments?
     * ;
     */
    public String translateClassType_lfno_classOrInterfaceType(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_classType_lfno_classOrInterfaceType)) {
            Log.error("RULE_classType_lfno_classOrInterfaceType error");
            return null;
        }

        String identifier = "";
        ParserRuleContext typeArgumentsCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode.getText();
                }
            }

            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_typeArguments) {
                    typeArgumentsCtx = (ParserRuleContext) child;
                }
            }
        }
        TypeArgumentsTranslate typeArgumentsTranslate = new TypeArgumentsTranslate();
        String typeArguments = typeArgumentsCtx == null ? "" : typeArgumentsTranslate.translateTypeArguments(typeArgumentsCtx);

        return identifier + " " + typeArguments;
    }
}
