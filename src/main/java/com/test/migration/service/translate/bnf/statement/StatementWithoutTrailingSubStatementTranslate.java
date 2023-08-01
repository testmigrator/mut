package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.TryStatementTranslate;
import com.test.migration.service.translate.bnf.statement.switchs.SwitchStatementTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class StatementWithoutTrailingSubStatementTranslate {
    /**
     * statementWithoutTrailingSubstatement
     * :  block
     * |  emptyStatement
     * |  expressionStatement
     * |  assertStatement
     * |  switchStatement
     * |  doStatement
     * |  breakStatement
     * |  continueStatement
     * |  returnStatement
     * |  synchronizedStatement
     * |  throwStatement
     * |  tryStatement
     * ;
     */
    public String translateStatementWithoutTrailingSubstatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_statementWithoutTrailingSubstatement) {
            Log.error("RULE_statementWithoutTrailingSubstatement error");
            return null;
        }
        ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(0);
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_block) {
            BlockTranslate translate = new BlockTranslate();
            return translate.translateBlock(childRuleContext);
        }
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_emptyStatement) {
            return ";";
        }
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_returnStatement) {
            ReturnStatementTranslate translate = new ReturnStatementTranslate();
            return translate.translateReturnStatement(childRuleContext);
        }
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_expressionStatement) {
            ExpressionStatementTranslate translate = new ExpressionStatementTranslate();
            return translate.translateExpressionStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_doStatement) {
            DoStatementTranslate translate = new DoStatementTranslate();
            return translate.translateDoStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_throwStatement) {
            ThrowStatementTranslate translate = new ThrowStatementTranslate();
            return translate.translateThrowStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_switchStatement) {
            SwitchStatementTranslate translate = new SwitchStatementTranslate();
            return translate.translateSwitchStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_tryStatement) {
            TryStatementTranslate translate = new TryStatementTranslate();
            return translate.translateTryStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_breakStatement) {
            BreakStatementTranslate translate = new BreakStatementTranslate();
            return translate.translateBreakStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_continueStatement) {
            return childRuleContext.getText();
        }

        /** 暂不考虑 **/
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_synchronizedStatement) {
            Log.error("RULE_synchronizedStatement 返回原结果");
            return ctx.getText();
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_assertStatement) {
            Log.error("RULE_assertStatement 返回原结果");
            return ctx.getText();
        }

        return null;
    }



}
