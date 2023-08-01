package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.PartMigrationProcessor;
import com.test.migration.service.translate.TranslateCodeCollector;
import com.test.migration.service.translate.TranslateHint;
import com.test.migration.service.translate.bnf.declaration.ClassDeclarationTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

public class BlockStatementTranslate {

    /**
     * blockStatement
     * :	localVariableDeclarationStatement
     * |	classDeclaration
     * |	statement
     * ;
     */
    public String translateBlockStatement(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_blockStatement) {
            Log.error("RULE_blockStatement error");
            return null;
        }

//        initHintIfNeed(ctx);

        String translateBlockStatement = null;
        ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(0);
        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_localVariableDeclarationStatement) {
            LocalVariableDeclarationStatementTranslate subTranslate = new LocalVariableDeclarationStatementTranslate();
            translateBlockStatement = subTranslate.translateLocalVariableDeclarationStatement(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_classDeclaration) {
            ClassDeclarationTranslate subTranslate = new ClassDeclarationTranslate();
            translateBlockStatement = subTranslate.translateClassDeclaration(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_statement) {
            StatementTranslate subTranslate = new StatementTranslate();
            translateBlockStatement = subTranslate.translateStatement(childRuleContext);
        }

        if (translateBlockStatement == null) {
            Log.error("translateBlockStatement error");
        }

        // 调整策略（部分迁移功能）
        String key = String.valueOf(ctx.getStart().getStartIndex());
        String modifyType = PartMigrationProcessor.blockStatementModifyMap.getOrDefault(key, "keep");
        if (StringUtils.equals(modifyType, "remove")) {
            // 清空bs收集到的hint
            TranslateHint.init();
            return "";
        }

        // 收集器
        collectBlockStatement(ctx, translateBlockStatement);

        return translateBlockStatement;
    }

    private static void collectBlockStatement(ParserRuleContext ctx, String translateBlockStatement) {
        TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode blockStatementTranslateCode = new TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode();
        blockStatementTranslateCode.translateCode = translateBlockStatement;
        blockStatementTranslateCode.tokenStartIndex = ctx.getStart().getStartIndex();
        blockStatementTranslateCode.tokenStopIndex = ctx.getStop().getStopIndex();
        blockStatementTranslateCode.misMatchCodes = TranslateHint.formatMisMatchCodes(TranslateHint.misMatchCodes);

        // 如果是完整迁移，则收集bs（区别于部分迁移）
        if (TranslateCodeCollector.isFullTranslate) {
            TranslateCodeCollector.blockStatementTranslateCodes.add(blockStatementTranslateCode);
        }
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
