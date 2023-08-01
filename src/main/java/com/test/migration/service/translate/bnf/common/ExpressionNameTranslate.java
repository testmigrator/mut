package com.test.migration.service.translate.bnf.common;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.replace.ExpressionNameReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

import java.util.List;

public class ExpressionNameTranslate {

    /**
     * expressionName
     * :	Identifier
     * |	ambiguousName '.' Identifier
     * ;
     */
    public String translateExpressionName(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_expressionName) {
            Log.error("RULE_expressionName error");
            return null;
        }

        if (ctx.getChildCount() == 1) {
            return ctx.getText();
        }

        AmbiguousNameTranslate ambiguousNameTranslate = new AmbiguousNameTranslate();
        String ambiguousName = ambiguousNameTranslate.translateAmbiguousName((ParserRuleContext) ctx.getChild(0));
        String identifier = ctx.getChild(2).getText();

        ExpressionNameReplace replace = new ExpressionNameReplace();
        List<String> originals = Lists.newArrayList(ambiguousName, ".", identifier);

        return replace.replaceStructure2(originals);
    }

}
