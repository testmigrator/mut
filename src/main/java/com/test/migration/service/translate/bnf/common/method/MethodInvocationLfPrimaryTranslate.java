package com.test.migration.service.translate.bnf.common.method;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ArgumentListTranslate;
import com.test.migration.service.translate.bnf.common.ExpressionNameTranslate;
import com.test.migration.service.translate.bnf.common.TypeArgumentsTranslate;
import com.test.migration.service.translate.replace.MethodInvocationLfPrimaryReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;

public class MethodInvocationLfPrimaryTranslate {
    /**
     * methodInvocation_lf_primary
     * :	'.' typeArguments? Identifier '(' argumentList? ')'
     * ;
     */
    public String translateMethodInvocationLfPrimary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_methodInvocation_lf_primary) {
            Log.error("RULE_methodInvocation_lf_primary error");
            return null;
        }
        String identifier = StringUtils.EMPTY;
        ParserRuleContext typeArgumentsCtx = null;
        ParserRuleContext argumentListCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof RuleContext) {
                RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
                if (childRuleContext.getRuleIndex() == Java8Parser.RULE_typeArguments) {
                    typeArgumentsCtx = (ParserRuleContext) childRuleContext;
                }
                if (childRuleContext.getRuleIndex() == Java8Parser.RULE_argumentList) {
                    argumentListCtx = (ParserRuleContext) childRuleContext;
                }
            }
            if (ctx.getChild(i) instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) ctx.getChild(i);
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode.getText();
                }
            }
        }

        TypeArgumentsTranslate typeArgumentsTranslate = new TypeArgumentsTranslate();
        String typeArguments = typeArgumentsCtx == null ? "" : typeArgumentsTranslate.translateTypeArguments(typeArgumentsCtx);

        ArgumentListTranslate argumentListTranslate = new ArgumentListTranslate();
        String argumentList = argumentListCtx == null ? "" : argumentListTranslate.translateArgumentList(argumentListCtx);

        MethodInvocationLfPrimaryReplace replace = new MethodInvocationLfPrimaryReplace();
        List<String> originals = Lists.newArrayList(".", typeArguments, identifier, " (", argumentList, ")");
        return replace.replaceStructure1(originals);
    }

}
