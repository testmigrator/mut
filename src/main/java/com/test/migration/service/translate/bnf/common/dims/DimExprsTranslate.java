package com.test.migration.service.translate.bnf.common.dims;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class DimExprsTranslate {

    /**
     * dimExprs
     * 	:	dimExpr dimExpr*
     * 	;
     */
    public String translateDimExprs(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_dimExprs)) {
            Log.error("RULE_dimExprs error");
            return null;
        }

        List<ParserRuleContext> dimExprCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);

            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_dimExpr) {
                dimExprCtxList.add((ParserRuleContext) childRuleContext);
            }
        }
        DimExprTranslate dimExprTranslate = new DimExprTranslate();

        List<String> list = dimExprCtxList.stream()
                .map(dimExprTranslate::translateDimExpr)
                .collect(Collectors.toList());
        return Joiner.on(" ").join(list);
    }
}
