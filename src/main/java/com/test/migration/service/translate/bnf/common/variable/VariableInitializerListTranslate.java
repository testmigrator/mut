package com.test.migration.service.translate.bnf.common.variable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class VariableInitializerListTranslate {

    /**
     * variableInitializerList
     * 	:	variableInitializer (',' variableInitializer)*
     * 	;
     */
    public String translateVariableInitializerList(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_variableInitializerList) {
            Log.error("RULE_variableInitializerList error");
            return null;
        }

        List<ParserRuleContext> variableInitializerCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childNode = (RuleContext) child;
            if (childNode.getRuleIndex() == Java8Parser.RULE_variableInitializer) {
                variableInitializerCtxList.add((ParserRuleContext) childNode);
            }
        }
        VariableInitializerTranslate variableInitializerTranslate = new VariableInitializerTranslate();
        List<String> list = variableInitializerCtxList.stream()
                .map(variableInitializerTranslate::translateVariableInitializer)
                .collect(Collectors.toList());

        return Joiner.on(",").join(list);

    }
}
