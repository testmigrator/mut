package com.test.migration.service.translate.bnf.common.primary;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.array.ArrayCreationExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class PrimaryTranslate {

    /**
     * primary
     * :	(	primaryNoNewArray_lfno_primary
     * |	arrayCreationExpression
     * )
     * (	primaryNoNewArray_lf_primary
     * )*
     * ;
     */
    public String translatePrimary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_primary) {
            Log.error("RULE_primary error");
            return null;
        }
        ParserRuleContext primaryNoNewArray_lfno_primaryCtx = null;
        ParserRuleContext arrayCreationExpressionCtx = null;
        List<ParserRuleContext> primaryNoNewArray_lf_primaryCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_primaryNoNewArray_lfno_primary) {
                primaryNoNewArray_lfno_primaryCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_arrayCreationExpression) {
                arrayCreationExpressionCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_primaryNoNewArray_lf_primary) {
                primaryNoNewArray_lf_primaryCtxList.add((ParserRuleContext) childRuleContext);
            }
        }

        PrimaryNoNewArrayLfNoPrimaryTranslate primaryNoNewArrayLfNoPrimaryTranslate = new PrimaryNoNewArrayLfNoPrimaryTranslate();
        String primaryNoNewArray_lfno_primary = primaryNoNewArray_lfno_primaryCtx == null ? ""
                : primaryNoNewArrayLfNoPrimaryTranslate.translatePrimaryNoNewArray_lfno_primary(primaryNoNewArray_lfno_primaryCtx);

        ArrayCreationExpressionTranslate arrayCreationExpressionTranslate = new ArrayCreationExpressionTranslate();
        String arrayCreationExpression = arrayCreationExpressionCtx == null ? ""
                : arrayCreationExpressionTranslate.translateArrayCreationExpression(arrayCreationExpressionCtx);

        PrimaryNoNewArrayLfPrimaryTranslate primaryNoNewArrayLfPrimaryTranslate = new PrimaryNoNewArrayLfPrimaryTranslate();
        List<String> primaryNoNewArrayLfPrimaryList = primaryNoNewArray_lf_primaryCtxList.stream()
                .map(primaryNoNewArrayLfPrimaryTranslate::translatePrimaryNoNewArray_lf_primary)
                .collect(Collectors.toList());
        String primaryNoNewArrayLfPrimaryListStr = Joiner.on(" ").join(primaryNoNewArrayLfPrimaryList);

        return primaryNoNewArray_lfno_primary + " " + arrayCreationExpression + " " + primaryNoNewArrayLfPrimaryListStr;
    }
}
