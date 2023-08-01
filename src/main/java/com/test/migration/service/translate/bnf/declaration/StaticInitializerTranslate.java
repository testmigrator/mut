package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.statement.BlockTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class StaticInitializerTranslate {

    /**
     * staticInitializer
     * 	:	'static' block
     * 	;
     * @param ctx
     * @return
     */
    public String translateStaticInitializer(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_staticInitializer)) {
            Log.error("RULE_staticInitializer error");
            return null;
        }

        BlockTranslate blockTranslate = new BlockTranslate();
        String block = blockTranslate.translateBlock((ParserRuleContext) ctx.getChild(1));
        return "static" + block;
    }
}
