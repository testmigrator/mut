package com.test.migration.service.translate.bnf.statement;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.unann.UnannTypeTranslate;
import com.test.migration.service.translate.bnf.common.variable.VariableDeclaratorListTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

/**
 * 目前支持下面类型的解析
 * int a = 1;  //直接赋值类型 directAssignment
 * int a = func(); //私有方法调用/继承类方法调用（这个暂不考虑），需要迁移调用到私有方法，如果私有方法迁移失败，则整体迁移失败
 * int a = c.func(1);//对象方法调用
 * int a = Test.func();//类静态方法调用
 * int a = this.a;//this访问
 * int a = new Test();// 创建类对象
 * int a = b * c;// 赋值语句本身是个表达式
 * <p>
 * ---暂不支持：
 * int a = Test::abc;
 * <p>
 * <p>
 * 当前限定的variableInitializer翻译：
 * 1.不支持arrayInitializer
 * 2.expression:
 * 不支持不含primaryNoNewArray_lfno_primary子节点的情况
 * 不支持arrayCreationExpression
 */
public class LocalVariableDeclarationStatementTranslate {
    /**
     * localVariableDeclarationStatement
     * :	localVariableDeclaration ';'
     * ;
     */
    public String translateLocalVariableDeclarationStatement(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_localVariableDeclarationStatement) {
            Log.error("localVariableDeclarationStatementContext error");
            return "";
        }
        ParserRuleContext localVariableDeclaration = (ParserRuleContext) ctx.getChild(0);

        return translateLocalVariableDeclaration(localVariableDeclaration) + ";";
    }

    /**
     * localVariableDeclaration
     * :	variableModifier* unannType variableDeclaratorList
     * ;
     */
    public String translateLocalVariableDeclaration(ParserRuleContext ctx) {
        if (ctx.getRuleIndex() != Java8Parser.RULE_localVariableDeclaration) {
            Log.error("RULE_localVariableDeclaration error");
            return null;
        }

        // find unanntype variableDeclaratorList
        ParserRuleContext unannTypeCtx = null;
        ParserRuleContext variableDeclaratorListCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_unannType) {
                unannTypeCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_variableDeclaratorList) {
                variableDeclaratorListCtx = (ParserRuleContext) childRuleContext;
            }
        }

        UnannTypeTranslate unannTypeTranslate = new UnannTypeTranslate();
        String unannType = unannTypeTranslate.translateUnannType(unannTypeCtx);

        VariableDeclaratorListTranslate variableDeclaratorListTranslate = new VariableDeclaratorListTranslate();
        String variableDeclaratorList = variableDeclaratorListTranslate.translateVariableDeclaratorList(variableDeclaratorListCtx);

        return unannType + " " + variableDeclaratorList;
    }
}
