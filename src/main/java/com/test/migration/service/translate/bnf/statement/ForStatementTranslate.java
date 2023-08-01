package com.test.migration.service.translate.bnf.statement;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.unann.UnannTypeTranslate;
import com.test.migration.service.translate.bnf.common.variable.VariableDeclaratorIdTranslate;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import com.test.migration.service.translate.bnf.expression.StatementExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class ForStatementTranslate {

    /**
     * forStatement
     * :	basicForStatement
     * |	enhancedForStatement
     * ;
     */
    public String translateForStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_forStatement) {
            Log.error("RULE_forStatement error");
            return null;
        }

        ParserRuleContext child = (ParserRuleContext) ctx.getChild(0);
        if (child.getRuleIndex() == Java8Parser.RULE_basicForStatement) {
            return translateBasicForStatement(child);
        }

        if (child.getRuleIndex() == Java8Parser.RULE_enhancedForStatement) {
            return translateEnhancedForStatement(child);
        }
        Log.error("RULE_forStatement error");
        return null;
    }


    /**
     * basicForStatement
     * :	'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
     * ;
     */
    public String translateBasicForStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_basicForStatement) {
            Log.error("RULE_basicForStatement error");
            return null;
        }
        ParserRuleContext forInitCtx = null;
        ParserRuleContext expressionCtx = null;
        ParserRuleContext forUpdateCtx = null;
        ParserRuleContext statementCtx = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_forInit) {
                forInitCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_expression) {
                expressionCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_forUpdate) {
                forUpdateCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_statement) {
                statementCtx = (ParserRuleContext) childRuleContext;
            }
        }

        String forInit = forInitCtx == null ? "" : translateForInit(forInitCtx);

        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        String expression = expressionCtx == null ? "" : expressionTranslate.translateExpression(expressionCtx);

        String forUpdate = forUpdateCtx == null ? "" : translateForUpdate(forUpdateCtx);

        StatementTranslate statementTranslate = new StatementTranslate();
        String statement = statementTranslate.translateStatement(statementCtx);

        return "for" + " (" + forInit + ";" + expression + ";" + forUpdate + ")" + statement;
    }

    /**
     * enhancedForStatement
     * :	'for' '(' variableModifier* unannType variableDeclaratorId ':' expression ')' statement
     * ;
     */
    public String translateEnhancedForStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_enhancedForStatement) {
            Log.error("RULE_enhancedForStatement error");
            return null;
        }

        ParserRuleContext unannTypeCtx = null;
        ParserRuleContext variableDeclaratorIdCtx = null;
        ParserRuleContext expressionCtx = null;
        ParserRuleContext statementCtx = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_unannType) {
                unannTypeCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_variableDeclaratorId) {
                variableDeclaratorIdCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_expression) {
                expressionCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_statement) {
                statementCtx = (ParserRuleContext) childRuleContext;
            }
        }

        UnannTypeTranslate unannTypeTranslate = new UnannTypeTranslate();
        String unannType = unannTypeTranslate.translateUnannType(unannTypeCtx);

        VariableDeclaratorIdTranslate variableDeclaratorIdTranslate = new VariableDeclaratorIdTranslate();
        String variableDeclaratorId = variableDeclaratorIdTranslate.translateVariableDeclaratorId(variableDeclaratorIdCtx);

        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        String expression = expressionTranslate.translateExpression(expressionCtx);

        StatementTranslate statementTranslate = new StatementTranslate();
        String statement = statementTranslate.translateStatement(statementCtx);

        return "for" + " (" + unannType + variableDeclaratorId + ":" + expression + ")" + statement;
    }


    /**
     * forInit
     * :	statementExpressionList
     * |	localVariableDeclaration
     * ;
     */
    public String translateForInit(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_forInit) {
            Log.error("RULE_forInit error");
            return null;
        }
        ParserRuleContext child = (ParserRuleContext) ctx.getChild(0);
        if (child.getRuleIndex() == Java8Parser.RULE_statementExpressionList) {
            return translateStatementExpressionList(child);
        }

        if (child.getRuleIndex() == Java8Parser.RULE_localVariableDeclaration) {
            LocalVariableDeclarationStatementTranslate translate = new LocalVariableDeclarationStatementTranslate();
            return translate.translateLocalVariableDeclaration(child);
        }
        Log.error("translateForInit error");
        return null;
    }


    /**
     * forUpdate
     * :	statementExpressionList
     * ;
     */
    public String translateForUpdate(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_forUpdate) {
            Log.error("RULE_forUpdate error");
            return null;
        }
        ParserRuleContext child = (ParserRuleContext) ctx.getChild(0);
        if (child.getRuleIndex() == Java8Parser.RULE_statementExpressionList) {
            return translateStatementExpressionList(child);
        }
        Log.error("translateForUpdate error");
        return null;
    }


    /**
     * statementExpressionList
     * :	statementExpression (',' statementExpression)*
     * ;
     */
    public String translateStatementExpressionList(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_statementExpressionList) {
            Log.error("RULE_statementExpressionList error");
            return null;
        }

        List<ParserRuleContext> statementExpressionCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_statementExpression) {
                statementExpressionCtxList.add((ParserRuleContext) childRuleContext);
            }
        }
        StatementExpressionTranslate statementExpressionTranslate = new StatementExpressionTranslate();
        List<String> statementExpressionList = statementExpressionCtxList.stream()
                .map(statementExpressionTranslate::translateStatementExpression)
                .collect(Collectors.toList());
        return Joiner.on(",").join(statementExpressionList);
    }


}
