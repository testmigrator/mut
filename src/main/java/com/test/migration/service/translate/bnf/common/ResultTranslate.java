package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.unann.UnannTypeTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class ResultTranslate {

    /**
     * result
     * 	:	unannType
     * 	|	'void'
     * 	;
     */
    public String translateResult(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_result) {
            Log.error("RULE_result error");
            return null;
        }

        ParseTree child = ctx.getChild(0);
        boolean isRuleContext = child instanceof RuleContext;
        if (!isRuleContext) {
            return ctx.getText();
        }

        UnannTypeTranslate unannTypeTranslate = new UnannTypeTranslate();
        return unannTypeTranslate.translateUnannType((ParserRuleContext) child);
    }
}
