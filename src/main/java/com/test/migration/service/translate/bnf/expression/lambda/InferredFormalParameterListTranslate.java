package com.test.migration.service.translate.bnf.expression.lambda;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class InferredFormalParameterListTranslate {

    /**
     * inferredFormalParameterList
     * :	Identifier (',' Identifier)*
     * ;
     */
    public String translateInferredFormalParameterList(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_inferredFormalParameterList) {
            Log.error("RULE_inferredFormalParameterList error");
            return null;
        }
        List<TerminalNode> identifierList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) ctx.getChild(i);
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifierList.add(terminalNode);
                }
            }
        }
        List<String> identifierStrList = identifierList.stream()
                .map(ParseTree::getText)
                .collect(Collectors.toList());

        return Joiner.on(",").join(identifierStrList);
    }

}
