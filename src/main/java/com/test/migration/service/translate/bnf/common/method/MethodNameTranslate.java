package com.test.migration.service.translate.bnf.common.method;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class MethodNameTranslate {

    public String translateMethodName(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_methodName) {
            Log.error("RULE_methodName error");
            return null;
        }
        return ctx.getText();
    }
}
