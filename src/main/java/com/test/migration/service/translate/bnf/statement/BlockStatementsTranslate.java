package com.test.migration.service.translate.bnf.statement;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class BlockStatementsTranslate {

    /**
     * blockStatements
     * :	blockStatement+
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateBlockStatements(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_blockStatements) {
            Log.error("RULE_blockStatements error");
            return null;
        }
        List<ParserRuleContext> blockStatementList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            blockStatementList.add((ParserRuleContext) child);
        }
        BlockStatementTranslate blockStatementTranslate = new BlockStatementTranslate();

        List<String> list = blockStatementList.stream()
                .map(blockStatementTranslate::translateBlockStatement)
                .collect(Collectors.toList());
        return Joiner.on("").join(list);
    }


}