package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.TestCodeContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.Log;

public class LabeledStatementTranslate {

    /**
     * labeledStatement
     * :	Identifier ':' statement
     * ;
     */
    public String translateLabeledStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_labeledStatement) {
            Log.error("RULE_labeledStatement error");
            return null;
        }

        ParseTree identifier = null;
        ParserRuleContext statementCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(0);
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode;
                }
            }
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_statement) {
                    statementCtx = (ParserRuleContext) child;
                }
            }
        }
        StatementTranslate statementTranslate = new StatementTranslate();

        String identifierStr = identifier == null ? "" : identifier.getText();
        String statement = statementCtx == null ? "" : statementTranslate.translateStatement(statementCtx);

        return identifierStr + ":" + statement;

    }
}
