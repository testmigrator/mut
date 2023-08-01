package com.test.migration.service.translate.bnf.common.method;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.statement.BlockTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class MethodBodyTranslate {
    /**
     * methodBody
     * :	block
     * |	';'
     * ;
     */
    public String translateMethodBody(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_methodBody) {
            Log.error("RULE_methodBody error");
            return null;
        }

        ParseTree child = ctx.getChild(0);
        boolean isRuleContext = child instanceof RuleContext;
        if (!isRuleContext) {
            return ctx.getText();
        }

        BlockTranslate blockTranslate = new BlockTranslate();
        String translateBlock = blockTranslate.translateBlock((ParserRuleContext) child);

        return translateBlock;
    }

}
