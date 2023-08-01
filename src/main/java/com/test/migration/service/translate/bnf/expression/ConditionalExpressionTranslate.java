package com.test.migration.service.translate.bnf.expression;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.entity.Constants;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

/**
 * 条件表达式解析和翻译
 */
public class ConditionalExpressionTranslate {

    /**
     * conditionalExpression
     * :	conditionalOrExpression
     * |	conditionalOrExpression '?' expression ':' conditionalExpression
     * ;
     *
     * @param ctx assignmentExpression
     * @return
     */
    public String translateConditionalExpression(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_conditionalExpression) {
            Log.error("RULE_conditionalExpression error");
            return null;
        }

        // conditionalOrExpression '?' expression ':' conditionalExpression
        if (ctx.getChildCount() > 1) {
            return translateConditionalOrExpressionWithTernary(ctx);
        }

        //conditionalOrExpression
        else {
            ParserRuleContext conditionalOrExpressionCtx = (ParserRuleContext) ctx.getChild(0);
            return translateConditionalOrExpression(conditionalOrExpressionCtx);
        }
    }

    /**
     * 三目运算符条件句翻译
     * conditionalOrExpression '?' expression ':' conditionalExpression
     *
     * @param ctx conditionalExpression
     * @return
     */
    public String translateConditionalOrExpressionWithTernary(ParserRuleContext ctx) {
        ParserRuleContext conditionalOrExpressionCtx = null;
        ParserRuleContext expressionCtx = null;
        ParserRuleContext conditionalExpressionCtx = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_conditionalOrExpression) {
                conditionalOrExpressionCtx = childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_expression) {
                expressionCtx = childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_conditionalExpression) {
                conditionalExpressionCtx = childRuleContext;
            }
        }
        String conditionalOrExpression = translateConditionalOrExpression(conditionalOrExpressionCtx);
        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        String expression = expressionTranslate.translateExpression(expressionCtx);
        String conditionalExpression = translateConditionalExpression(conditionalExpressionCtx);

        //conditionalOrExpression '?' expression ':' conditionalExpression
        return conditionalOrExpression + "?" + expression + ":" + conditionalExpression;
    }

    /**
     * conditionalOrExpression
     * :	conditionalAndExpression
     * |	conditionalOrExpression '||' conditionalAndExpression
     * ;
     */
    public String translateConditionalOrExpression(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_conditionalOrExpression) {
            Log.error("conditionalOrExpressionRule error");
            return null;
        }

        // conditionalOrExpression '||' conditionalAndExpression
        if (ctx.getChildCount() > 1) {
            return translateConditionalOrExpressionWithOr(ctx);
        }

        // conditionalAndExpression
        else {
            return translateConditionalAndExpression((ParserRuleContext) ctx.getChild(0));
        }
    }

    /**
     * conditionalAndExpression
     * <p>
     * conditionalAndExpression
     * :	inclusiveOrExpression
     * |	conditionalAndExpression '&&' inclusiveOrExpression
     * ;
     *
     * @param ctx ConditionalAndExpression
     * @return
     */
    public String translateConditionalAndExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_conditionalAndExpression) {
            Log.error("conditionalAndExpressionRule error");
            return null;
        }
        // conditionalAndExpression '&&' inclusiveOrExpression
        if (ctx.getChildCount() > 1) {
            return translateConditionalAndExpressionWithAnd(ctx);
        }
        // inclusiveOrExpression
        else {
            return translateInclusiveOrExpression((ParserRuleContext) ctx.getChild(0));
        }
    }

    /**
     * inclusiveOrExpression
     * <p>
     * inclusiveOrExpression
     * :	exclusiveOrExpression
     * |	inclusiveOrExpression '|' exclusiveOrExpression
     * ;
     *
     * @param ctx RULE_conditionalAndExpression
     * @return
     */
    public String translateInclusiveOrExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_inclusiveOrExpression) {
            Log.error("inclusiveOrExpressionRule error");
            return null;
        }
        //inclusiveOrExpression '|' exclusiveOrExpression
        if (ctx.getChildCount() > 1) {
            return translateInclusiveOrExpressionWithOr(ctx);
        }
        //exclusiveOrExpression
        else {
            return translateExclusiveOrExpression((ParserRuleContext) ctx.getChild(0));
        }
    }

    /**
     * exclusiveOrExpression
     * :	andExpression
     * |	exclusiveOrExpression '^' andExpression
     * ;
     *
     * @param ctx RULE_inclusiveOrExpression
     * @return
     */
    public String translateExclusiveOrExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_exclusiveOrExpression) {
            Log.error("exclusiveOrExpressionRule error");
            return null;
        }
        // exclusiveOrExpression '^' andExpression
        if (ctx.getChildCount() > 1) {
            return translateExclusiveOrExpressionWithHat(ctx);
        }
        // andExpression
        else {
            return translateAndExpression((ParserRuleContext) ctx.getChild(0));
        }
    }

    /**
     * andExpression
     * :	equalityExpression
     * |	andExpression '&' equalityExpression
     * ;
     *
     * @param ctx RULE_exclusiveOrExpression
     * @return
     */
    public String translateAndExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_andExpression) {
            Log.error("andExpressionRule error");
            return null;
        }
        //andExpression '&' equalityExpression
        if (ctx.getChildCount() > 1) {
            return translateAndExpressionWithAnd(ctx);
        }
        //equalityExpression
        else {
            return translateEqualityExpression((ParserRuleContext) ctx.getChild(0));
        }
    }

    /**
     * equalityExpression
     * :	relationalExpression
     * |	equalityExpression '==' relationalExpression
     * |	equalityExpression '!=' relationalExpression
     * ;
     *
     * @param ctx RULE_andExpression
     * @return
     */
    public String translateEqualityExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_equalityExpression) {
            Log.error("equalityExpressionRule error");
            return null;
        }
        // relationalExpression
        if (ctx.getChildCount() == 1) {
            return translateRelationalExpression((ParserRuleContext) ctx.getChild(0));
        }

        // equalityExpression '==' relationalExpression

        boolean existEqual = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (StringUtils.equals(ctx.getChild(i).getText(), "==")) {
                existEqual = true;
            }
        }
        if (existEqual) {
            return translateEqualityExpressionWithEqual(ctx);
        }
        //equalityExpression '!=' relationalExpression
        return translateEqualityExpressionWithNotEqual(ctx);
    }

    public String translateEqualityExpressionWithNotEqual(ParserRuleContext ctx) {
        String equalityExpression = "";
        String relationalExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_equalityExpression) {
                equalityExpression = translateEqualityExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_relationalExpression) {
                relationalExpression = translateRelationalExpression((ParserRuleContext) childRuleContext);
            }
        }
        return equalityExpression + " != " + relationalExpression;
    }

    public String translateEqualityExpressionWithEqual(ParserRuleContext ctx) {
        String equalityExpression = "";
        String relationalExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_equalityExpression) {
                equalityExpression = translateEqualityExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_relationalExpression) {
                relationalExpression = translateRelationalExpression((ParserRuleContext) childRuleContext);
            }
        }
        return equalityExpression + " == " + relationalExpression;
    }

    /**
     * relationalExpression
     * :	shiftExpression
     * |	relationalExpression '<' shiftExpression
     * |	relationalExpression '>' shiftExpression
     * |	relationalExpression '<=' shiftExpression
     * |	relationalExpression '>=' shiftExpression
     * |	relationalExpression 'instanceof' referenceType
     * ;
     *
     * @param ctx equalityExpressionRule
     * @return
     */
    public String translateRelationalExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_relationalExpression) {
            Log.error("relationalExpressionRule error");
            return null;
        }

        // shiftExpression
        if (ctx.getChildCount() == 1) {
            return translateShiftExpression((ParserRuleContext) ctx.getChild(0));
        }

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (StringUtils.equals(ctx.getChild(i).getText(), "<")) {
                // relationalExpression '<' shiftExpression
                return translateRelationalExpressionWithLess(ctx);
            }
            if (StringUtils.equals(ctx.getChild(i).getText(), ">")) {
                // relationalExpression '>' shiftExpression
                return translateRelationalExpressionWithLarge(ctx);
            }
            if (StringUtils.equals(ctx.getChild(i).getText(), "<=")) {
                // relationalExpression '<=' shiftExpression
                return translateRelationalExpressionWithLessEqual(ctx);
            }
            if (StringUtils.equals(ctx.getChild(i).getText(), ">=")) {
                // relationalExpression '>=' shiftExpression
                return translateRelationalExpressionWithLargeEqual(ctx);
            }
            if (StringUtils.equals(ctx.getChild(i).getText(), "instanceof")) {
                // relationalExpression 'instanceof' shiftExpression
                return translateRelationalExpressionWithInstanceof(ctx);
            }
        }
        Log.error(" translateRelationalExpression error");
        return null;
    }

    public String translateRelationalExpressionWithInstanceof(ParserRuleContext ctx) {
        return ctx.getText();
    }

    public String translateRelationalExpressionWithLargeEqual(ParserRuleContext ctx) {
        String relationalExpression = "";
        String shiftExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_relationalExpression) {
                relationalExpression = translateRelationalExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_shiftExpression) {
                shiftExpression = translateShiftExpression((ParserRuleContext) childRuleContext);
            }
        }
        return relationalExpression + " >= " + shiftExpression;
    }

    public String translateRelationalExpressionWithLessEqual(ParserRuleContext ctx) {
        String relationalExpression = "";
        String shiftExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_relationalExpression) {
                relationalExpression = translateRelationalExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_shiftExpression) {
                shiftExpression = translateShiftExpression((ParserRuleContext) childRuleContext);
            }
        }
        return relationalExpression + " <= " + shiftExpression;
    }

    public String translateRelationalExpressionWithLarge(ParserRuleContext ctx) {
        String relationalExpression = "";
        String shiftExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_relationalExpression) {
                relationalExpression = translateRelationalExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_shiftExpression) {
                shiftExpression = translateShiftExpression((ParserRuleContext) childRuleContext);
            }
        }
        return relationalExpression + " > " + shiftExpression;
    }

    /**
     * relationalExpression '<' shiftExpression
     *
     * @param ctx relationalExpressionRule
     * @return
     */
    public String translateRelationalExpressionWithLess(ParserRuleContext ctx) {
        String relationalExpression = "";
        String shiftExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_relationalExpression) {
                relationalExpression = translateRelationalExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_shiftExpression) {
                shiftExpression = translateShiftExpression((ParserRuleContext) childRuleContext);
            }
        }
        return relationalExpression + " < " + shiftExpression;
    }

    /**
     * shiftExpression
     * :	additiveExpression
     * <p>
     * // todo 下面这几个 暂不支持
     * |	shiftExpression '<' '<' additiveExpression
     * |	shiftExpression '>' '>' additiveExpression
     * |	shiftExpression '>' '>' '>' additiveExpression
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateShiftExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_shiftExpression) {
            Log.error("shiftExpressionRule error" + ctx.getText());
            return null;
        }

        if (ctx.getChildCount() > 1) {
            Log.error("暂不支持位运算符的条件表达式！" + ctx.getText());
            return ctx.getText();
        }
        // additiveExpression
        return translateAdditiveExpression((ParserRuleContext) ctx.getChild(0));
    }

    /**
     * additiveExpression
     * :	multiplicativeExpression
     * |	additiveExpression '+' multiplicativeExpression
     * |	additiveExpression '-' multiplicativeExpression
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateAdditiveExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_additiveExpression) {
            Log.error("additiveExpressionRule error");
            return null;
        }

        if (ctx.getChildCount() == 1) {
            return translateMultiplicativeExpression((ParserRuleContext) ctx.getChild(0));
        }

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (StringUtils.equals(ctx.getChild(i).getText(), "+")) {
                // 	additiveExpression '+' multiplicativeExpression
                return translateAdditiveExpressionWithPlus(ctx);
            }
            if (StringUtils.equals(ctx.getChild(i).getText(), Constants.SEPARATOR.BAR)) {
                // 	additiveExpression '-' multiplicativeExpression
                return translateAdditiveExpressionWithMinus(ctx);
            }
        }

        return null;
    }

    /**
     * additiveExpression '+' multiplicativeExpression
     */
    public String translateAdditiveExpressionWithMinus(ParserRuleContext ctx) {
        String additiveExpression = "";
        String multiplicativeExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_additiveExpression) {
                additiveExpression = translateAdditiveExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_multiplicativeExpression) {
                multiplicativeExpression = translateMultiplicativeExpression((ParserRuleContext) childRuleContext);
            }
        }
        return additiveExpression + " - " + multiplicativeExpression;
    }

    public String translateAdditiveExpressionWithPlus(ParserRuleContext ctx) {
        String additiveExpression = "";
        String multiplicativeExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_additiveExpression) {
                additiveExpression = translateAdditiveExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_multiplicativeExpression) {
                multiplicativeExpression = translateMultiplicativeExpression((ParserRuleContext) childRuleContext);
            }
        }
        return additiveExpression + " + " + multiplicativeExpression;
    }

    /**
     * multiplicativeExpression
     * :	unaryExpression
     * |	multiplicativeExpression '*' unaryExpression
     * |	multiplicativeExpression '/' unaryExpression
     * |	multiplicativeExpression '%' unaryExpression
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateMultiplicativeExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_multiplicativeExpression) {
            Log.error("multiplicativeExpressionRule 没找到，不科学:" + ctx.getText());
            return null;
        }
        //unaryExpression
        if (ctx.getChildCount() == 1) {
            return translateUnaryExpression((ParserRuleContext) ctx.getChild(0));
        }
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (StringUtils.equals(ctx.getChild(i).getText(), "*")) {
                // 	multiplicativeExpression '*' unaryExpression
                return translateMultiplicativeExpressionWithMultiply(ctx);
            }
            if (StringUtils.equals(ctx.getChild(i).getText(), "/")) {
                // 	multiplicativeExpression '/' unaryExpression
                return translateMultiplicativeExpressionWithDivide(ctx);
            }
            if (StringUtils.equals(ctx.getChild(i).getText(), "%")) {
                // 	multiplicativeExpression '%' unaryExpression
                return translateMultiplicativeExpressionWithRemainder(ctx);
            }
        }
        Log.error("translateAdditiveExpression error");
        return null;
    }

    /**
     * multiplicativeExpression '%' unaryExpression
     *
     * @param ctx
     * @return
     */
    public String translateMultiplicativeExpressionWithRemainder(ParserRuleContext ctx) {
        String multiplicativeExpression = "";
        String unaryExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_multiplicativeExpression) {
                multiplicativeExpression = translateMultiplicativeExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_unaryExpression) {
                unaryExpression = translateUnaryExpression((ParserRuleContext) childRuleContext);
            }
        }
        return multiplicativeExpression + " % " + unaryExpression;
    }

    public String translateMultiplicativeExpressionWithDivide(ParserRuleContext ctx) {
        String multiplicativeExpression = "";
        String unaryExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_multiplicativeExpression) {
                multiplicativeExpression = translateMultiplicativeExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_unaryExpression) {
                unaryExpression = translateUnaryExpression((ParserRuleContext) childRuleContext);
            }
        }
        return multiplicativeExpression + " / " + unaryExpression;
    }

    public String translateMultiplicativeExpressionWithMultiply(ParserRuleContext ctx) {
        String multiplicativeExpression = "";
        String unaryExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_multiplicativeExpression) {
                multiplicativeExpression = translateMultiplicativeExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_unaryExpression) {
                unaryExpression = translateUnaryExpression((ParserRuleContext) childRuleContext);
            }
        }
        return multiplicativeExpression + " * " + unaryExpression;
    }

    /**
     * unaryExpression
     * :	preIncrementExpression
     * |	preDecrementExpression
     * |	'+' unaryExpression
     * |	'-' unaryExpression
     * |	unaryExpressionNotPlusMinus
     * ;
     *
     * @param ctx 多种：multiplicativeExpression， preIncrementExpressionRule 等等
     * @return
     */
    public String translateUnaryExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_unaryExpression) {
            Log.error("unaryExpressionRule error");
            return null;
        }

        if (ctx.getChildCount() == 1) {
            // 肯定是RuleContext
            RuleContext childRule = (RuleContext) ctx.getChild(0);

            // preIncrementExpression
            if (childRule.getRuleIndex() == Java8Parser.RULE_preIncrementExpression) {
                return translatePreIncrementExpression((ParserRuleContext) childRule);
            }

            // preDecrementExpression
            if (childRule.getRuleIndex() == Java8Parser.RULE_preDecrementExpression) {
                return translatePreDecrementExpression((ParserRuleContext) childRule);
            }

            //unaryExpressionNotPlusMinus
            if (childRule.getRuleIndex() == Java8Parser.RULE_unaryExpressionNotPlusMinus) {
                return translateUnaryExpressionNotPlusMinus((ParserRuleContext) childRule);
            }
        }

        for (int i = 0; i < ctx.getChildCount(); i++) {
            //'+' unaryExpression
            if (StringUtils.equals(ctx.getChild(i).getText(), "+")) {
                return translateUnaryExpressionWithPlus(ctx);
            }
            //'-' unaryExpression
            if (StringUtils.equals(ctx.getChild(i).getText(), Constants.SEPARATOR.BAR)) {
                return translateUnaryExpressionWithMinus(ctx);
            }
        }

        Log.error("translateUnaryExpression error");
        return null;
    }

    public String translateUnaryExpressionWithMinus(ParserRuleContext ctx) {
        String unaryExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_unaryExpression) {
                unaryExpression = translateUnaryExpression((ParserRuleContext) childRuleContext);
            }
        }
        return "-" + unaryExpression;
    }

    public String translateUnaryExpressionWithPlus(ParserRuleContext ctx) {
        String unaryExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_unaryExpression) {
                unaryExpression = translateUnaryExpression((ParserRuleContext) childRuleContext);
            }
        }
        return "+" + unaryExpression;
    }

    /**
     * unaryExpressionNotPlusMinus
     * :	postfixExpression
     * |	'~' unaryExpression
     * |	'!' unaryExpression
     * |	castExpression
     * ;
     *
     * @param ctx unaryExpressionRule
     * @return
     */
    public String translateUnaryExpressionNotPlusMinus(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_unaryExpressionNotPlusMinus) {
            Log.error("unaryExpressionNotPlusMinusRule error");
            return null;
        }

        if (ctx.getChildCount() == 1) {
            // 肯定是RuleContext
            RuleContext childRule = (RuleContext) ctx.getChild(0);
            if (childRule.getRuleIndex() == Java8Parser.RULE_postfixExpression) {
                PostfixExpressionTranslate postfixExpressionTranslate = new PostfixExpressionTranslate();
                return postfixExpressionTranslate.translatePostfixExpression((ParserRuleContext) childRule);
            }

            if (childRule.getRuleIndex() == Java8Parser.RULE_castExpression) {
                return translateCastExpression((ParserRuleContext) childRule);
            }
        }

        //'~' unaryExpression
        if (StringUtils.equals(ctx.getChild(0).getText(), "~")) {
            return "~" + translateUnaryExpression((ParserRuleContext) ctx.getChild(1));
        }
        //'!' unaryExpression
        if (StringUtils.equals(ctx.getChild(0).getText(), "!")) {
            return "!" + translateUnaryExpression((ParserRuleContext) ctx.getChild(1));
        }

        Log.error("translateUnaryExpressionNotPlusMinus error" + ctx.getText());
        return null;
    }

    /**
     * TODO 暂不支持
     * castExpression
     * 	:	'(' primitiveType ')' unaryExpression
     * 	|	'(' referenceType additionalBound* ')' unaryExpressionNotPlusMinus
     * 	|	'(' referenceType additionalBound* ')' lambdaExpression
     * 	;
     */
    public String translateCastExpression(ParserRuleContext ctx) {
//        Log.error("暂不支持translateCastExpression");
        return ctx.getText();
    }

    /**
     * @param ctx unaryExpressionRule
     * @return
     */
    public String translatePreDecrementExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_preDecrementExpression) {
            Log.error("preDecrementExpressionRule error" + ctx.getText());
            return null;
        }
        return "--" + translateUnaryExpression((ParserRuleContext) ctx.getChild(1));
    }

    /**
     * preIncrementExpression
     * :	'++' unaryExpression
     * ;
     *
     * @return
     */
    public String translatePreIncrementExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_preIncrementExpression) {
            Log.error("preIncrementExpressionRule error" + ctx.getText());
            return null;
        }
        return "++" + translateUnaryExpression((ParserRuleContext) ctx.getChild(1));
    }

    /**
     * andExpression '&' equalityExpression
     *
     * @return andExpressionRule
     */
    public String translateAndExpressionWithAnd(ParserRuleContext ctx) {
        String andExpression = "";
        String equalityExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_andExpression) {
                andExpression = translateAndExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_equalityExpression) {
                equalityExpression = translateEqualityExpression((ParserRuleContext) childRuleContext);
            }
        }
        return andExpression + " & " + equalityExpression;
    }

    /**
     * exclusiveOrExpression '^' andExpression
     *
     * @param ctx exclusiveOrExpressionRule
     * @return
     */
    public String translateExclusiveOrExpressionWithHat(ParserRuleContext ctx) {
        String exclusiveOrExpression = "";
        String andExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_exclusiveOrExpression) {
                exclusiveOrExpression = translateExclusiveOrExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_andExpression) {
                andExpression = translateAndExpression((ParserRuleContext) childRuleContext);
            }
        }
        return exclusiveOrExpression + " ^ " + andExpression;
    }

    /**
     * inclusiveOrExpression '|' exclusiveOrExpression
     *
     * @param ctx inclusiveOrExpressionRule
     * @return
     */
    public String translateInclusiveOrExpressionWithOr(ParserRuleContext ctx) {
        String inclusiveOrExpression = "";
        String exclusiveOrExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_inclusiveOrExpression) {
                inclusiveOrExpression = translateInclusiveOrExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_exclusiveOrExpression) {
                exclusiveOrExpression = translateExclusiveOrExpression((ParserRuleContext) childRuleContext);
            }
        }
        return inclusiveOrExpression + " | " + exclusiveOrExpression;
    }

    /**
     * conditionalAndExpression '&&' inclusiveOrExpression
     *
     * @param ctx conditionalAndExpressionRule
     * @return
     */
    public String translateConditionalAndExpressionWithAnd(ParserRuleContext ctx) {
        String conditionalAndExpression = "";
        String inclusiveOrExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_inclusiveOrExpression) {
                inclusiveOrExpression = translateInclusiveOrExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_conditionalAndExpression) {
                conditionalAndExpression = translateConditionalAndExpression((ParserRuleContext) childRuleContext);
            }
        }
        return conditionalAndExpression + " && " + inclusiveOrExpression;
    }

    /**
     * conditionalOrExpression '||' conditionalAndExpression
     *
     * @param ctx ConditionalOrExpression
     * @return
     */
    public String translateConditionalOrExpressionWithOr(ParserRuleContext ctx) {
        String conditionalOrExpression = "";
        String conditionalAndExpression = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) child;
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_conditionalOrExpression) {
                conditionalOrExpression = translateConditionalOrExpression((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_conditionalAndExpression) {
                conditionalAndExpression = translateConditionalAndExpression((ParserRuleContext) childRuleContext);
            }
        }
        return conditionalOrExpression + " || " + conditionalAndExpression;
    }

    /**
     * postIncrementExpression
     * :	postfixExpression '++'
     * ;
     *
     * @param ctx
     * @return
     */
    public String translatePostIncrementExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_postIncrementExpression) {
            Log.error("RULE_postIncrementExpression error" + ctx.getText());
            return null;
        }
        PostfixExpressionTranslate postfixExpressionTranslate = new PostfixExpressionTranslate();
        return postfixExpressionTranslate.translatePostfixExpression((ParserRuleContext) ctx.getChild(0)) + "++";
    }

    public String translatePostDecrementExpression(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_postDecrementExpression) {
            Log.error("RULE_postDecrementExpression error" + ctx.getText());
            return null;
        }
        PostfixExpressionTranslate postfixExpressionTranslate = new PostfixExpressionTranslate();
        return postfixExpressionTranslate.translatePostfixExpression((ParserRuleContext) ctx.getChild(0)) + "--";
    }


}
