package com.test.migration.service.translate.bnf.common.method;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.TranslateCodeCollector;
import com.test.migration.service.translate.TranslateHint;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.stream.Collectors;

public class MethodDeclarationTranslate {

    /**
     * methodDeclaration
     * :  methodModifier* methodHeader methodBody
     * ;
     */
    public String translateMethodDeclaration(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_methodDeclaration)) {
            Log.error("RULE_methodDeclaration error");
            return null;
        }

        // 初始化hint
        initHintIfNeed(ctx);

        //筛选出methodBody
        ParserRuleContext methodHeaderRule = null;
        ParserRuleContext methodBodyRule = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof RuleContext &&
                    ((RuleContext) ctx.getChild(i)).getRuleIndex() == Java8Parser.RULE_methodHeader) {
                methodHeaderRule = (ParserRuleContext) ctx.getChild(i);
            }
            if (ctx.getChild(i) instanceof RuleContext &&
                    ((RuleContext) ctx.getChild(i)).getRuleIndex() == Java8Parser.RULE_methodBody) {
                methodBodyRule = (ParserRuleContext) ctx.getChild(i);
            }
        }

        if (methodBodyRule == null) {
            Log.error("methodBodyRule error");
            return null;
        }

        MethodHeaderTranslate methodHeaderTranslate = new MethodHeaderTranslate();
        String methodHeader = methodHeaderTranslate.translateMethodHeader(methodHeaderRule);

        // 判断是否待翻译method中的内部方法，如果是就不收集header了
        boolean isInnerMethod = ctx.getStart().getLine() > TranslateCodeCollector.MethodTranslateCode.methodStartLine
                && ctx.getStop().getLine() < TranslateCodeCollector.MethodTranslateCode.methodEndLine;
        if (!isInnerMethod) {
            // 收集methodHeader信息
            TranslateCodeCollector.MethodTranslateCode.MethodHeaderTranslateCode methodHeaderTranslateCode = new TranslateCodeCollector.MethodTranslateCode.MethodHeaderTranslateCode();
            methodHeaderTranslateCode.translateCode = methodHeader;
            methodHeaderTranslateCode.misMatchCodes = TranslateHint.formatMisMatchCodes(TranslateHint.misMatchCodes);
            TranslateCodeCollector.methodHeaderTranslateCode = methodHeaderTranslateCode;
        }

        MethodBodyTranslate methodBodyTranslate = new MethodBodyTranslate();
        String methodBody = methodBodyTranslate.translateMethodBody(methodBodyRule);

        return methodHeader + " " + methodBody;
    }

    private static void initHintIfNeed(ParserRuleContext ctx) {
        int ctxStartLine = ctx.getStart().getStartIndex();
        int ctxEndLine = ctx.getStop().getStopIndex();
        boolean isSubBlockStatement = ctxStartLine >= TranslateHint.startLine && ctxEndLine <= TranslateHint.endLine;
        if (!isSubBlockStatement) {
            TranslateHint.init();
            TranslateHint.startLine = ctxStartLine;
            TranslateHint.endLine = ctxEndLine;
            TranslateHint.misMatchCodes.add(TranslateHint.BS_HINT_TAG);
        }
    }

}