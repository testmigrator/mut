package com.test.migration.service.translate.bnf.common.cls;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ArgumentListTranslate;
import com.test.migration.service.translate.replace.ClassInstanceCreationExpressionLfPrimaryReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;

public class ClassInstanceCreationExpressionLfPrimaryTranslate {
    /**
     * classInstanceCreationExpression_lf_primary
     * :	'.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
     * ;
     */
    public String translateClassInstanceCreationExpression_lf_primary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_classInstanceCreationExpression_lf_primary) {
            Log.error("RULE_classInstanceCreationExpression_lf_primary error");
            return null;
        }

        // '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        // 简化为 '.' 'new' Identifier '(' argumentList? ')' classBody?
        // 获取第一个Identifier
        String identifier = "";
        // 获取argumentList
        ParserRuleContext argumentListRule = null;
        ParserRuleContext classBodyRule = null;
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

        ClassInstanceCreationExpressionLfPrimaryReplace replace = new ClassInstanceCreationExpressionLfPrimaryReplace();
        List<String> originals = Lists.newArrayList("." , "new " , identifier , " (" , argumentList , ")" , classBody);
        return replace.replaceStructure1(originals);
    }
}
