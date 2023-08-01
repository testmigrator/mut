package com.test.migration.service.translate.bnf.common.array;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.cls.ClassOrInterfaceTypeTranslate;
import com.test.migration.service.translate.bnf.common.dims.DimExprsTranslate;
import com.test.migration.service.translate.bnf.common.dims.DimsTranslate;
import com.test.migration.service.translate.bnf.common.variable.TypeVariableTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class ArrayCreationExpressionTranslate {

    /**
     * arrayCreationExpression
     * :	'new' primitiveType dimExprs dims?
     * |	'new' classOrInterfaceType dimExprs dims?
     * |	'new' primitiveType dims arrayInitializer
     * |	'new' classOrInterfaceType dims arrayInitializer
     * ;
     */
    public String translateArrayCreationExpression(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_arrayCreationExpression)) {
            Log.error("RULE_arrayCreationExpression error");
            return null;
        }

        // 1.获取各个类型子节点
        ParserRuleContext primitiveTypeCtx = null;
        ParserRuleContext dimExprsCtx = null;
        ParserRuleContext dimsCtx = null;
        ParserRuleContext classOrInterfaceTypeCtx = null;
        ParserRuleContext arrayInitializerCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_primitiveType) {
                    primitiveTypeCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_dimExprs) {
                    dimExprsCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_dims) {
                    dimsCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_classOrInterfaceType) {
                    classOrInterfaceTypeCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_arrayInitializer) {
                    arrayInitializerCtx = (ParserRuleContext) child;
                }
            }
        }

        DimsTranslate dimsTranslate = new DimsTranslate();
        DimExprsTranslate dimExprsTranslate = new DimExprsTranslate();
        ClassOrInterfaceTypeTranslate classOrInterfaceTypeTranslate = new ClassOrInterfaceTypeTranslate();
        ArrayInitializerTranslate arrayInitializerTranslate = new ArrayInitializerTranslate();

        String dims = dimsCtx == null ? "" : dimsTranslate.translateDims(dimsCtx);
        String dimExprs = dimExprsCtx == null ? "" : dimExprsTranslate.translateDimExprs(dimExprsCtx);
        String classOrInterfaceType = classOrInterfaceTypeCtx == null ? "" : classOrInterfaceTypeTranslate.translateClassOrInterfaceType(classOrInterfaceTypeCtx);
        String arrayInitializer = arrayInitializerCtx == null ? "" : arrayInitializerTranslate.translateArrayInitializer(arrayInitializerCtx);
        String primitiveType = primitiveTypeCtx == null ? "" : primitiveTypeCtx.getText();

        //'new' primitiveType dimExprs dims?
        if (primitiveTypeCtx != null && dimExprsCtx != null) {
            return "new " + primitiveType + " " + dimExprs + " " + dims;
        }

        //'new' primitiveType dims arrayInitializer
        if (primitiveTypeCtx != null && dimsCtx != null) {
            return "new " + primitiveType + " " + dims + " " + arrayInitializer;
        }

        //'new' classOrInterfaceType dimExprs dims?
        if (classOrInterfaceTypeCtx != null && dimExprsCtx != null) {
            return "new " + classOrInterfaceType + " " + dimExprs + " " + dims;
        }

        //'new' classOrInterfaceType dims arrayInitializer
        if (classOrInterfaceTypeCtx != null && dimsCtx != null) {
            return "new " + classOrInterfaceType + " " + dims + " " + arrayInitializer;
        }

        return null;
    }
}
