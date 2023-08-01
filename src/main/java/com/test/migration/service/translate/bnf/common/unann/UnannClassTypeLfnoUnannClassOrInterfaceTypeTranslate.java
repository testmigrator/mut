package com.test.migration.service.translate.bnf.common.unann;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.TypeArgumentsTranslate;
import com.test.migration.service.translate.replace.UnannClassTypeLfnoUnannClassOrInterfaceTypeReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.Log;

import java.util.List;

public class UnannClassTypeLfnoUnannClassOrInterfaceTypeTranslate {

    /**
     * unannClassType_lfno_unannClassOrInterfaceType
     * :	Identifier typeArguments?
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateUnannClassTypeLfnoUnannClassOrInterfaceType(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_unannClassType_lfno_unannClassOrInterfaceType) {
            Log.error("RULE_unannClassType_lfno_unannClassOrInterfaceType error");
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


        UnannClassTypeLfnoUnannClassOrInterfaceTypeReplace replace = new UnannClassTypeLfnoUnannClassOrInterfaceTypeReplace();
        List<String> originals = Lists.newArrayList(identifier, typeArguments);

        return replace.replaceStructure1(originals);
    }
}
