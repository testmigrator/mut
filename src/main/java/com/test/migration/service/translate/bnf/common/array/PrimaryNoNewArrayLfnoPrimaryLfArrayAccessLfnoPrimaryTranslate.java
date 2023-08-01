package com.test.migration.service.translate.bnf.common.array;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class PrimaryNoNewArrayLfnoPrimaryLfArrayAccessLfnoPrimaryTranslate {

    /**
     * primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary
     * 	:
     * 	;
     * @param ctx
     * @return
     */
    public String translatePrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary)) {
            Log.info("RULE_primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary error");
            return null;
        }
        return "";
    }
}
