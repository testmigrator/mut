package com.test.migration.service.translate.bnf.common.primary;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.replace.FieldAccessLfnoPrimaryReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;

public class FieldAccessLfnoPrimaryTranslate {

    /**
     * fieldAccess_lfno_primary
     * :	'super' '.' Identifier
     * |	typeName '.' 'super' '.' Identifier
     * ;
     * <p>
     * eg：
     * super.xxx;
     * FieldAccessLfnoPrimaryTranslate.super.xxx;
     */
    public String translateFieldAccessLfnoPrimary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_fieldAccess_lfno_primary) {
            Log.error("RULE_fieldAccess_lfno_primary error");
            return null;
        }
        FieldAccessLfnoPrimaryReplace replace = new FieldAccessLfnoPrimaryReplace();
        // 'super' '.' Identifier
        if (ctx.getChildCount() == 3) {
            return ctx.getText();
        } else {
            // typeName '.' 'super' '.' Identifier
            RuleContext ruleContext = (RuleContext) ctx.getChild(0);
            String typeName = ruleContext.getText();
            String identifier = ctx.getChild(ctx.getChildCount() - 1).getText();
            List<String> originals = Lists.newArrayList(typeName, ".", "super", ".", identifier);
            return replace.replaceStructure2(originals);
        }
    }
}
