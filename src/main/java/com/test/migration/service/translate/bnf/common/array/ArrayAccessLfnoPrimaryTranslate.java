package com.test.migration.service.translate.bnf.common.array;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ExpressionNameTranslate;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class ArrayAccessLfnoPrimaryTranslate {

    /**
     * arrayAccess_lfno_primary
     * :	(	expressionName '[' expression ']'
     * |	primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary '[' expression ']'
     * )
     * (	primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary '[' expression ']'
     * )*
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateArrayAccess_lfno_primary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_arrayAccess_lfno_primary) {
            Log.error("RULE_arrayAccess_lfno_primary error");
            return null;
        }

        ParserRuleContext expressionNameCtx = null;
        ParserRuleContext expressionCtx = null;
        ParserRuleContext primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryCtx = null;
        ParserRuleContext primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryCtx = null;

        ExpressionNameTranslate expressionNameTranslate = new ExpressionNameTranslate();
        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        PrimaryNoNewArrayLfnoPrimaryLfnoArrayAccessLfnoPrimaryTranslate primaryNoNewArrayLfnoPrimaryLfnoArrayAccessLfnoPrimaryTranslate =
                new PrimaryNoNewArrayLfnoPrimaryLfnoArrayAccessLfnoPrimaryTranslate();
        PrimaryNoNewArrayLfnoPrimaryLfArrayAccessLfnoPrimaryTranslate primaryNoNewArrayLfnoPrimaryLfArrayAccessLfnoPrimaryTranslate =
                new PrimaryNoNewArrayLfnoPrimaryLfArrayAccessLfnoPrimaryTranslate();
        String expressionNameStr = "";
        String primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary = "";
        List<String> primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_expressionName) {
                    expressionNameCtx = (ParserRuleContext) child;
                    expressionCtx = (ParserRuleContext) ctx.getChild(i + 2);
                    //expressionName '[' expression ']'
                    String expressionName = expressionNameTranslate.translateExpressionName(expressionNameCtx);
                    String expression = expressionTranslate.translateExpression(expressionCtx);
                    expressionNameStr = expressionName + "[" + expression + "]";
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary) {
                    primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryCtx = (ParserRuleContext) child;
                    expressionCtx = (ParserRuleContext) ctx.getChild(i + 2);
                    String primaryNoNewArrayLfnoPrimaryLfnoArrayAccessLfnoPrimary = primaryNoNewArrayLfnoPrimaryLfnoArrayAccessLfnoPrimaryTranslate
                            .translatePrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryCtx);
                    String expression = expressionTranslate.translateExpression(expressionCtx);
                    primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary = primaryNoNewArrayLfnoPrimaryLfnoArrayAccessLfnoPrimary + "[" + expression + "]";
                }

                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary) {
                    primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryCtx = (ParserRuleContext) child;
                    expressionCtx = (ParserRuleContext) ctx.getChild(i + 2);
                    String primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary = primaryNoNewArrayLfnoPrimaryLfArrayAccessLfnoPrimaryTranslate
                            .translatePrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryCtx);
                    String expression = expressionTranslate.translateExpression(expressionCtx);
                    primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryList.add(primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary + "[" + expression + "]");
                }
            }
        }

        String primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary = Joiner.on(" ").join(primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryList);

        return expressionNameStr + " "
                + primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary + " "
                + primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary;
    }
}
