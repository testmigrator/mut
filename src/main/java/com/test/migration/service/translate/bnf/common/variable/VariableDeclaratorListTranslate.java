package com.test.migration.service.translate.bnf.common.variable;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.array.ArrayInitializerTranslate;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;

public class VariableDeclaratorListTranslate {


    /**
     * variableDeclaratorList
     * :	variableDeclarator (',' variableDeclarator)*
     * ;
     */
    public String translateVariableDeclaratorList(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_variableDeclaratorList) {
            Log.error("RULE_variableDeclaratorList error");
            return null;
        }
        List<ParserRuleContext> variableDeclaratorCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childNode = (RuleContext) child;
            if (childNode.getRuleIndex() == Java8Parser.RULE_variableDeclarator) {
                variableDeclaratorCtxList.add((ParserRuleContext) childNode);
            }
        }

        StringBuilder variableDeclarators = new StringBuilder();
        VariableDeclaratorTranslate variableDeclaratorTranslate = new VariableDeclaratorTranslate();
        for (int i = 0; i < variableDeclaratorCtxList.size(); i++) {
            if (i == variableDeclaratorCtxList.size() - 1) {
                variableDeclarators.append(variableDeclaratorTranslate.translateVariableDeclarator(variableDeclaratorCtxList.get(i)));
            } else {
                variableDeclarators.append(variableDeclaratorTranslate.translateVariableDeclarator(variableDeclaratorCtxList.get(i))).append(",");
            }
        }

        return variableDeclarators.toString();
    }


}
