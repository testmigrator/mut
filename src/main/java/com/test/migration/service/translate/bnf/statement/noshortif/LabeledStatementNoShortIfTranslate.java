package com.test.migration.service.translate.bnf.statement.noshortif;

import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.statement.StatementTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.Log;

public class LabeledStatementNoShortIfTranslate {

    /**
     * labeledStatementNoShortIf
     * 	:	Identifier ':' statementNoShortIf
     * 	;
     */
    public String translateLabeledStatementNoShortIf(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_labeledStatementNoShortIf) {
            Log.error("RULE_labeledStatementNoShortIf error");
            return null;
        }

        ParseTree identifier = null;
        ParserRuleContext statementNoShortIfCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(0);
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode;
                }
            }
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_statementNoShortIf) {
                    statementNoShortIfCtx = (ParserRuleContext) child;
                }
            }
        }
        StatementNoShortIfTranslate statementTranslate = new StatementNoShortIfTranslate();

        String identifierStr = identifier == null ? "" : identifier.getText();
        String statementNoShortIf = statementNoShortIfCtx == null ? "" : statementTranslate.translateStatementNoShortIf(statementNoShortIfCtx);

        return identifierStr + ":" + statementNoShortIf;

    }
}
