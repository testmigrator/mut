package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.statement.BlockTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class InstanceInitializerTranslate {

    /**
     * instanceInitializer
     * 	:	block
     * 	;
     * @param ctx
     * @return
     */
    public String translateInstanceInitializer(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_instanceInitializer)) {
            Log.error("RULE_instanceInitializer error");
            return null;
        }

        ParserRuleContext ctxChild = (ParserRuleContext) ctx.getChild(0);
        BlockTranslate blockTranslate = new BlockTranslate();
        String block = blockTranslate.translateBlock(ctxChild);

        return block;
    }
}
