package com.test.migration.service.translate.bnf.common.unann;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.dims.DimsTranslate;
import com.test.migration.service.translate.replace.UnannArrayTypeReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

import java.util.List;

public class UnannArrayTypeTranslate {

    /**
     * unannArrayType
     * :	unannPrimitiveType dims
     * |	unannClassOrInterfaceType dims
     * |	unannTypeVariable dims
     * ;
     */
    public String translateUnannArrayType(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_unannArrayType) {
            Log.error("RULE_unannArrayType error");
            return "";
        }

        // 1.获取各个类型子节点
        ParserRuleContext unannPrimitiveTypeCtx = null;
        ParserRuleContext unannClassOrInterfaceTypeCtx = null;
        ParserRuleContext unannTypeVariableCtx = null;
        ParserRuleContext dimsCtx = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_unannPrimitiveType) {
                    unannPrimitiveTypeCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_unannClassOrInterfaceType) {
                    unannClassOrInterfaceTypeCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_unannTypeVariable) {
                    unannTypeVariableCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_dims) {
                    dimsCtx = (ParserRuleContext) child;
                }
            }
        }
        UnannPrimitiveTypeTranslate unannPrimitiveTypeTranslate = new UnannPrimitiveTypeTranslate();
        UnannClassOrInterfaceTypeTranslate unannClassOrInterfaceTypeTranslate = new UnannClassOrInterfaceTypeTranslate();
        UnannTypeVariableTranslate unannTypeVariableTranslate = new UnannTypeVariableTranslate();
        DimsTranslate dimsTranslate = new DimsTranslate();

        String unannPrimitiveType = unannPrimitiveTypeCtx == null ? "" : unannPrimitiveTypeTranslate.translateUnannPrimitiveType(unannPrimitiveTypeCtx);
        String unannClassOrInterfaceType = unannClassOrInterfaceTypeCtx == null ? "" : unannClassOrInterfaceTypeTranslate.translateUnannClassOrInterfaceType(unannClassOrInterfaceTypeCtx);
        String unannTypeVariable = unannTypeVariableCtx == null ? "" : unannTypeVariableTranslate.translateUnannTypeVariable(unannTypeVariableCtx);
        String dims = dimsCtx == null ? "" : dimsTranslate.translateDims(dimsCtx);

        // 2. 判断第一个孩子节点的类型
        RuleContext firstChild = (RuleContext) ctx.getChild(0);
        int ruleIndex = firstChild.getRuleIndex();
        UnannArrayTypeReplace unannArrayTypeReplace = new UnannArrayTypeReplace();
        //unannPrimitiveType dims
        if (ruleIndex == Java8Parser.RULE_unannPrimitiveType) {
            List<String> originals = Lists.newArrayList(unannPrimitiveType, dims);
            return unannArrayTypeReplace.replaceStructure1(originals);
        }

        //unannClassOrInterfaceType dims
        if (ruleIndex == Java8Parser.RULE_unannClassOrInterfaceType) {
            List<String> originals = Lists.newArrayList(unannClassOrInterfaceType, dims);
            return unannArrayTypeReplace.replaceStructure2(originals);
        }

        //unannTypeVariable dims
        if (ruleIndex == Java8Parser.RULE_unannTypeVariable) {
            List<String> originals = Lists.newArrayList(unannTypeVariable, dims);
            return unannArrayTypeReplace.replaceStructure2(originals);
        }

        Log.error("translateUnannArrayType error");
        return null;
    }

}
