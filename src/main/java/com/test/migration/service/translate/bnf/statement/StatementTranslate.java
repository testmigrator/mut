package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class StatementTranslate {

    /**
     * statement
     * :  statementWithoutTrailingSubstatement
     * |  labeledStatement
     * |  ifThenStatement
     * |  ifThenElseStatement
     * |  whileStatement
     * |  forStatement
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_statement) {
            Log.error("RULE_statement error");
            return null;
        }
        ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(0);
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_statementWithoutTrailingSubstatement) {
            StatementWithoutTrailingSubStatementTranslate translate = new StatementWithoutTrailingSubStatementTranslate();
            return translate.translateStatementWithoutTrailingSubstatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_labeledStatement) {
            LabeledStatementTranslate translate = new LabeledStatementTranslate();
            return translate.translateLabeledStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_ifThenStatement) {
            IfThenStatementTranslate ifThenStatementTranslate = new IfThenStatementTranslate();
            return ifThenStatementTranslate.translateIfThenStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_ifThenElseStatement) {
            IfThenElseStatementTranslate translate = new IfThenElseStatementTranslate();
            return translate.translateIfThenElseStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_whileStatement) {
            WhileStatementTranslate whileStatementTranslate = new WhileStatementTranslate();
            return whileStatementTranslate.translateWhileStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_forStatement) {
            ForStatementTranslate forStatementTranslate = new ForStatementTranslate();
            return forStatementTranslate.translateForStatement(childRuleContext);
        }

        Log.error("translateStatement error");
        return null;
    }

}
