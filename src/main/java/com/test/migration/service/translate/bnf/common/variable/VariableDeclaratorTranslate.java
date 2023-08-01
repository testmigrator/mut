package com.test.migration.service.translate.bnf.common.variable;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

public class VariableDeclaratorTranslate {

    /**
     * variableDeclarator
     * :	variableDeclaratorId ('=' variableInitializer)?
     * ;
     */
    public String translateVariableDeclarator(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_variableDeclarator) {
            Log.error("RULE_variableDeclarator error");
            return null;
        }

        String variableDeclaratorId = "";
        String variableInitializer = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_variableDeclaratorId) {
                VariableDeclaratorIdTranslate variableDeclaratorIdTranslate = new VariableDeclaratorIdTranslate();
                variableDeclaratorId = variableDeclaratorIdTranslate.translateVariableDeclaratorId((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_variableInitializer) {
                VariableInitializerTranslate translate = new VariableInitializerTranslate();
                variableInitializer = translate.translateVariableInitializer((ParserRuleContext) childRuleContext);
            }
        }

        // TODO： 暂未解决多个'='问题（int a =2, b=3;）
        return StringUtils.isBlank(variableInitializer) ? variableDeclaratorId
                : variableDeclaratorId + "=" + variableInitializer;
    }

}
