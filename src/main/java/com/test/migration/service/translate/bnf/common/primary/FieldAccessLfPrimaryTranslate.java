package com.test.migration.service.translate.bnf.common.primary;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class FieldAccessLfPrimaryTranslate {

    /**
     * fieldAccess_lf_primary
     * :	'.' Identifier
     * ;
     */
    public String translateFieldAccessLfPrimary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_fieldAccess_lf_primary) {
            Log.error("RULE_fieldAccess_lf_primary error");
            return null;
        }
        return ctx.getText();
    }
}
