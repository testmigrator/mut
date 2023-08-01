package com.test.migration.service.translate.bnf.common.primary;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.ArgumentListTranslate;
import com.test.migration.service.translate.bnf.common.ExpressionNameTranslate;
import com.test.migration.service.translate.bnf.common.ReferenceTypeTranslate;
import com.test.migration.service.translate.bnf.common.TypeArgumentsTranslate;
import com.test.migration.service.translate.bnf.common.array.ArrayTypeTranslate;
import com.test.migration.service.translate.bnf.common.cls.ClassTypeTranslate;
import com.test.migration.service.translate.bnf.common.method.MethodNameTranslate;
import com.test.migration.service.translate.bnf.common.method.TypeNameTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;

public class MethodReferenceLfnoPrimaryTranslate {

    /**
     * methodReference_lfno_primary
     * :	expressionName '::' typeArguments? Identifier
     * |	referenceType '::' typeArguments? Identifier
     * |	'super' '::' typeArguments? Identifier
     * |	typeName '.' 'super' '::' typeArguments? Identifier
     * |	classType '::' typeArguments? 'new'
     * |	arrayType '::' 'new'
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateMethodReferenceLfnoPrimary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_methodReference_lfno_primary) {
            Log.error("RULE_methodReference_lfno_primary error");
            return null;
        }

        // 1.获取各个类型子节点
        ParseTree identifier = null;
        ParserRuleContext expressionNameCtx = null;
        ParserRuleContext typeArgumentsCtx = null;
        ParserRuleContext referenceTypeCtx = null;
        ParserRuleContext typeNameCtx = null;
        ParserRuleContext classTypeCtx = null;
        ParserRuleContext arrayTypeCtx = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_expressionName) {
                    expressionNameCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_typeArguments) {
                    typeArgumentsCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_referenceType) {
                    referenceTypeCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_typeName) {
                    typeNameCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_classType) {
                    classTypeCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_arrayType) {
                    arrayTypeCtx = (ParserRuleContext) child;
                }
            }
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode;
                }
            }
        }

        ExpressionNameTranslate expressionNameTranslate = new ExpressionNameTranslate();
        TypeArgumentsTranslate typeArgumentsTranslate = new TypeArgumentsTranslate();
        ReferenceTypeTranslate referenceTypeTranslate = new ReferenceTypeTranslate();
        TypeNameTranslate typeNameTranslate = new TypeNameTranslate();
        ClassTypeTranslate classTypeTranslate = new ClassTypeTranslate();
        ArrayTypeTranslate arrayTypeTranslate = new ArrayTypeTranslate();

        String identifierStr = identifier == null ? "" : identifier.getText();
        String expressionName = expressionNameCtx == null ? "" : expressionNameTranslate.translateExpressionName(expressionNameCtx);
        String typeArguments = typeArgumentsCtx == null ? "" : typeArgumentsTranslate.translateTypeArguments(typeArgumentsCtx);
        String referenceType = referenceTypeCtx == null ? "" : referenceTypeTranslate.translateReferenceType(referenceTypeCtx);
        String typeName = typeNameCtx == null ? "" : typeNameTranslate.translateTypeName(typeNameCtx);
        String classType = classTypeCtx == null ? "" : classTypeTranslate.translateClassType(classTypeCtx);
        String arrayType = arrayTypeCtx == null ? "" : arrayTypeTranslate.translateArrayType(arrayTypeCtx);


        ParseTree firstChild = ctx.getChild(0);
        boolean isRuleContext = firstChild instanceof RuleContext;
        if (!isRuleContext) {
            // 'super' '::' typeArguments? Identifier
            return "super" + "::" + typeArguments + " " + identifierStr;
        } else {
            int ruleIndex = ((RuleContext) firstChild).getRuleIndex();
            if (ruleIndex == Java8Parser.RULE_expressionName) {
                //expressionName '::' typeArguments? Identifier
                return expressionName + "::" + typeArguments + " " + identifierStr;
            }

            if (ruleIndex == Java8Parser.RULE_referenceType) {
                //referenceType '::' typeArguments? Identifier
                return referenceType + "::" + typeArguments + " " + identifierStr;
            }

            if (ruleIndex == Java8Parser.RULE_typeName) {
                //typeName '.' 'super' '::' typeArguments? Identifier
                return typeName + "." + "super" + "::" + typeArguments + " " + identifierStr;
            }

            if (ruleIndex == Java8Parser.RULE_classType) {
                //classType '::' typeArguments? 'new'
                return classType + "::" + typeArguments + "new";
            }

            if (ruleIndex == Java8Parser.RULE_arrayType) {
                //arrayType '::' 'new'
                return arrayType + "::" + "new";
            }
        }

        return null;
    }
}
