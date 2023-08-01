package com.test.migration.service.translate.bnf.common.array;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class primaryNoNewArrayLfPrimaryLfArrayAccessLfPrimaryTranslate {

    /**
     * primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary
     * 	:
     * 	;
     */
    public String translatePrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary)) {
            Log.info("RULE_primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary error");
            return null;
        }

        return "";
    }
}
