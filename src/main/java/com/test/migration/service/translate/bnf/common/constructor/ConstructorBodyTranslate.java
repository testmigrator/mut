package com.test.migration.service.translate.bnf.common.constructor;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.statement.BlockStatementsTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class ConstructorBodyTranslate {

    /**
     * constructorBody
     * :	'{' explicitConstructorInvocation? blockStatements? '}'
     * ;
     */
    public String translateConstructorBody(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_constructorBody)) {
            Log.error("RULE_constructorBody error");
            return null;
        }

        ParserRuleContext explicitConstructorInvocationCtx = null;
        ParserRuleContext blockStatementsCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_explicitConstructorInvocation) {
                    explicitConstructorInvocationCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_blockStatements) {
                    blockStatementsCtx = (ParserRuleContext) child;
                }
            }
        }
        ExplicitConstructorInvocationTranslate explicitConstructorInvocationTranslate = new ExplicitConstructorInvocationTranslate();
        BlockStatementsTranslate blockStatementsTranslate = new BlockStatementsTranslate();

        String explicitConstructorInvocation = explicitConstructorInvocationCtx == null ? "" :
                explicitConstructorInvocationTranslate.translateExplicitConstructorInvocation(explicitConstructorInvocationCtx);
        String blockStatements = blockStatementsCtx == null ? "" : blockStatementsTranslate.translateBlockStatements(blockStatementsCtx);

        return "{ " + explicitConstructorInvocation + " " + blockStatements + " }";
    }
}
