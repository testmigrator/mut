package com.test.migration.service.translate.bnf.statement.switchs;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

public class SwitchLabelTranslate {

    /**
     * switchLabel
     * :	'case' constantExpression ':'
     * |	'case' enumConstantName ':'
     * |	'default' ':'
     * ;
     */
    public String translateSwitchLabel(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_switchLabel) {
            Log.error("RULE_switchLabel error");
            return null;
        }

        ParserRuleContext constantExpressionCtx = null;
        ParserRuleContext enumConstantNameCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_constantExpression) {
                constantExpressionCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_enumConstantName) {
                enumConstantNameCtx = (ParserRuleContext) childRuleContext;
            }
        }
        if (constantExpressionCtx != null) {
            return "case" + translateConstantExpression(constantExpressionCtx) + ":";
        }
        if (enumConstantNameCtx != null) {
            return "case" + translateEnumConstantName(enumConstantNameCtx) + ":";
        }
        return "default:";
    }


    /**
     * constantExpression
     * :	expression
     * ;
     */
    public String translateConstantExpression(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_constantExpression) {
            Log.error("RULE_constantExpression error");
            return null;
        }
        ParserRuleContext ctxChild = (ParserRuleContext) ctx.getChild(0);

        ExpressionTranslate expressionTranslate = new ExpressionTranslate();

        return expressionTranslate.translateExpression(ctxChild);
    }

    /**
     * enumConstantName
     * :	Identifier
     * ;
     */
    public String translateEnumConstantName(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_constantExpression) {
            Log.error("RULE_constantExpression error");
            return null;
        }

        return ctx.getText();
    }


}
