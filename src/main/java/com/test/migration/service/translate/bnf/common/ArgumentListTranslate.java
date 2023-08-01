package com.test.migration.service.translate.bnf.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class ArgumentListTranslate {
    /**
     * argumentList
     * 	:	expression (',' expression)*
     * 	;
     * @param ctx
     * @return
     */
    public String translateArgumentList(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_argumentList) {
            Log.error("RULE_argumentList error");
            return null;
        }
        //组合成expression列表
        List<ParserRuleContext> expressionCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childNode = (RuleContext) child;
            if (childNode.getRuleIndex() == Java8Parser.RULE_expression) {
                expressionCtxList.add((ParserRuleContext) childNode);
            }
        }
        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        List<String> expressions = expressionCtxList.stream()
                .map(expressionTranslate::translateExpression)
                .collect(Collectors.toList());
        return Joiner.on(",").join(expressions);
    }
}
