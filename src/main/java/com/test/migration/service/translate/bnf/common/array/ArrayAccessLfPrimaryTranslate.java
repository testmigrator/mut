package com.test.migration.service.translate.bnf.common.array;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

import java.util.List;

public class ArrayAccessLfPrimaryTranslate {

    /**
     * arrayAccess_lf_primary
     * 	:	(	primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary '[' expression ']'
     * 		)
     * 		(	primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary '[' expression ']'
     * 		)*
     * 	;
     * @param ctx
     * @return
     */
    public String translateArrayAccessLfPrimary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_arrayAccess_lf_primary) {
            Log.error("RULE_arrayAccess_lf_primary error");
            return null;
        }

        ParserRuleContext expressionCtx = null;
        ParserRuleContext primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryCtx = null;
        ParserRuleContext primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryCtx = null;

        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        PrimaryNoNewArrayLfPrimaryLfnoArrayAccessLfPrimaryTranslate primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryTranslate =
                new PrimaryNoNewArrayLfPrimaryLfnoArrayAccessLfPrimaryTranslate();
        primaryNoNewArrayLfPrimaryLfArrayAccessLfPrimaryTranslate primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryTranslate =
                new primaryNoNewArrayLfPrimaryLfArrayAccessLfPrimaryTranslate();

        String primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary = "";
        List<String> primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary) {
                    primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryCtx = (ParserRuleContext) child;
                    expressionCtx = (ParserRuleContext) ctx.getChild(i + 2);
                    String primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryTmp = primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryTranslate
                            .translatePrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryCtx);
                    String expression = expressionTranslate.translateExpression(expressionCtx);
                    primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary = primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryTmp + "[" + expression + "]";
                }

                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary) {
                    primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryCtx = (ParserRuleContext) child;
                    expressionCtx = (ParserRuleContext) ctx.getChild(i + 2);
                    String primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary = primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryTranslate
                            .translatePrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryCtx);
                    String expression = expressionTranslate.translateExpression(expressionCtx);
                    primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryList.add(primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary + "[" + expression + "]");
                }
            }
        }

        String primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary = Joiner.on(" ").join(primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryList);

        return primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary + " "
                + primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary;

    }
}
