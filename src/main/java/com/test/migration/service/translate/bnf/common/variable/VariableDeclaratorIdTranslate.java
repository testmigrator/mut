package com.test.migration.service.translate.bnf.common.variable;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class VariableDeclaratorIdTranslate {

    /**
     * 变量标识符，直接返回text
     * 比如：int a = 3   返回的是a
     */
    public String translateVariableDeclaratorId(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_variableDeclaratorId) {
            Log.error("RULE_variableDeclaratorId error");
            return null;
        }
        return ctx.getText();
    }

}
