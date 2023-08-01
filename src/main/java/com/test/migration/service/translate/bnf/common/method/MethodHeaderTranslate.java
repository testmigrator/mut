package com.test.migration.service.translate.bnf.common.method;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ResultTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class MethodHeaderTranslate {

    /**
     * methodHeader
     * :	result methodDeclarator throws_?
     * |	typeParameters annotation* result methodDeclarator throws_?
     * ;
     */
    public String translateMethodHeader(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_methodHeader) {
            Log.error("RULE_methodHeader error");
            return null;
        }

        ParserRuleContext resultRule = null;
        ParserRuleContext methodDeclaratorRule = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_result) {
                resultRule = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_methodDeclarator) {
                methodDeclaratorRule = (ParserRuleContext) childRuleContext;
            }
        }

        ResultTranslate resultTranslate = new ResultTranslate();
        String result = resultTranslate.translateResult(resultRule);

        MethodDeclaratorTranslate methodDeclaratorTranslate = new MethodDeclaratorTranslate();
        String methodDeclarator = methodDeclaratorTranslate.translateMethodDeclarator(methodDeclaratorRule);

        return result + " " + methodDeclarator;
    }

}
