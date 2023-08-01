package com.test.migration.service.translate.bnf.common.cls;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ArgumentListTranslate;
import com.test.migration.service.translate.bnf.common.ExpressionNameTranslate;
import com.test.migration.service.translate.bnf.common.primary.PrimaryTranslate;
import com.test.migration.service.translate.replace.ClassInstanceCreationExpressionReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;

public class ClassInstanceCreationExpressionTranslate {

    /**
     * classInstanceCreationExpression
     * :	'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
     * |	expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
     * |	primary '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
     * ;
     * <p>
     * typeArguments, typeArgumentsOrDiamond:范型，均不转换
     */
    public String translateClassInstanceCreationExpression(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_classInstanceCreationExpression) {
            Log.error("RULE_classInstanceCreationExpression error");
            return null;
        }
        ParseTree child = ctx.getChild(0);
        boolean isRuleContext = child instanceof RuleContext;

        // 获取argumentList
        ParserRuleContext argumentListRule = null;
        ParserRuleContext classBodyRule = null;
        String identifier = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof RuleContext &&
                    ((RuleContext) ctx.getChild(i)).getRuleIndex() == Java8Parser.RULE_argumentList) {
                argumentListRule = (ParserRuleContext) ctx.getChild(i);
            }
            if (ctx.getChild(i) instanceof RuleContext &&
                    ((RuleContext) ctx.getChild(i)).getRuleIndex() == Java8Parser.RULE_classBody) {
                classBodyRule = (ParserRuleContext) ctx.getChild(i);
            }

            if (StringUtils.isBlank(identifier) && ctx.getChild(i) instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) ctx.getChild(i);
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode.getText();
                }
            }
        }

        String argumentList = "";
        //argumentListRule可能为空
        if (argumentListRule != null) {
            ArgumentListTranslate argumentListTranslate = new ArgumentListTranslate();
            argumentList = argumentListTranslate.translateArgumentList(argumentListRule);
        }

        String classBody = "";
        if (classBodyRule != null) {
            ClassBodyTranslate classBodyTranslate = new ClassBodyTranslate();
            classBody = classBodyTranslate.translateClassBody(classBodyRule);
        }

        ClassInstanceCreationExpressionReplace replace = new ClassInstanceCreationExpressionReplace();

        //'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        // 简化转换为：'new' Identifier '(' argumentList? ')' classBody?
        if (!isRuleContext) {
            List<String> originals = Lists.newArrayList("new ", identifier, " (", argumentList, ")", classBody);
            return replace.replaceStructure1(originals);
        }

        // expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        // 简化转换为：expressionName '.' 'new' Identifier '(' argumentList? ')' classBody?
        if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_expressionName) {
            ExpressionNameTranslate expressionNameTranslate = new ExpressionNameTranslate();
            String expressionName = expressionNameTranslate.translateExpressionName((ParserRuleContext) child);

            List<String> originals = Lists.newArrayList(expressionName, ".", "new ", identifier, " (", argumentList, ")", classBody);
            return replace.replaceStructure2(originals);
        }


        // primary '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        // 简化转换为：primary '.' 'new' Identifier '(' argumentList? ')' classBody?
        if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_primary) {
            PrimaryTranslate primaryTranslate = new PrimaryTranslate();
            String primary = primaryTranslate.translatePrimary((ParserRuleContext) child);

            List<String> originals = Lists.newArrayList(primary, ".", "new ", identifier, " (", argumentList, ")", classBody);
            return replace.replaceStructure3(originals);
        }

        Log.error("translateClassInstanceCreationExpression error");
        return null;
    }

}
