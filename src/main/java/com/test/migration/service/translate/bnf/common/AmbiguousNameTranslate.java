package com.test.migration.service.translate.bnf.common;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.replace.AmbiguousNameReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

import java.util.List;

public class AmbiguousNameTranslate {

    /**
     * ambiguousName
     * :	Identifier
     * |	ambiguousName '.' Identifier
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateAmbiguousName(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_ambiguousName) {
            Log.error("RULE_ambiguousName error");
            return null;
        }
        if (ctx.getChildCount() == 1) {
            return ctx.getText();
        }

        String ambiguousName = translateAmbiguousName((ParserRuleContext) ctx.getChild(0));
        String identifier = ctx.getChild(2).getText();
        AmbiguousNameReplace replace = new AmbiguousNameReplace();
        List<String> originals = Lists.newArrayList(ambiguousName, ".", identifier);

        return replace.replaceStructure2(originals);
    }

}
