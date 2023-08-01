package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.array.ArrayAccessTranslate;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class AssignmentTranslate {

    /**
     * assignment
     * :	leftHandSide assignmentOperator expression
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateAssignment(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_assignment) {
            Log.error("RULE_assignment error");
            return null;
        }
        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        return translateLeftHandSide((ParserRuleContext) ctx.getChild(0))
                + translateAssignmentOperator((ParserRuleContext) ctx.getChild(1))
                + expressionTranslate.translateExpression((ParserRuleContext) ctx.getChild(2));
    }

    /**
     * leftHandSide
     * :	expressionName
     * |	fieldAccess
     * |	arrayAccess
     * ;
     */
    public String translateLeftHandSide(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_leftHandSide) {
            Log.error("RULE_leftHandSide error");
            return null;
        }
        ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(0);
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_expressionName) {
            ExpressionNameTranslate translate = new ExpressionNameTranslate();
            return translate.translateExpressionName(childRuleContext);
        }
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_fieldAccess) {
            FieldAccessTranslate translate = new FieldAccessTranslate();
            return translate.translateFieldAccess(childRuleContext);
        }
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_arrayAccess) {
            ArrayAccessTranslate translate = new ArrayAccessTranslate();
            return translate.translateArrayAccess(childRuleContext);
        }

        Log.error("translateLeftHandSide error");
        return null;
    }

    /**
     * assignmentOperator
     * :	'='
     * |	'*='
     * |	'/='
     * |	'%='
     * |	'+='
     * |	'-='
     * |	'<<='
     * |	'>>='
     * |	'>>>='
     * |	'&='
     * |	'^='
     * |	'|='
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateAssignmentOperator(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_assignmentOperator) {
            Log.error("RULE_assignmentOperator error");
            return null;
        }
        return ctx.getText();
    }


}
