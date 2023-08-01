package com.test.migration.service.translate.bnf.common.method;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class TypeNameTranslate {
    public String translateTypeName(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_typeName) {
            Log.error("RULE_typeName error");
            return null;
        }

        return ctx.getText();
    }
}
