package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.statement.BlockTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class TryWithResourcesStatementTranslate {

    /**
     * tryWithResourcesStatement
     * 	:	'try' resourceSpecification block catches? finally_?
     * 	;
     */
    public String translateTryWithResourcesStatement(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_tryWithResourcesStatement)) {
            Log.error("RULE_tryWithResourcesStatement error");
            return null;
        }

        ParserRuleContext blockCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_block) {
                blockCtx = childRuleContext;
            }
        }
        BlockTranslate blockTranslate = new BlockTranslate();
        return blockTranslate.translateBlock(blockCtx);

    }

}
