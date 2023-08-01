package com.test.migration.service.translate.bnf.common.primary;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.LiteralTranslate;
import com.test.migration.service.translate.bnf.common.array.ArrayAccessLfnoPrimaryTranslate;
import com.test.migration.service.translate.bnf.common.cls.ClassInstanceCreationExpressionLfnoPrimaryTranslate;
import com.test.migration.service.translate.bnf.common.method.MethodInvocationLfnoPrimaryTranslate;
import com.test.migration.service.translate.bnf.common.method.TypeNameTranslate;
import com.test.migration.service.translate.bnf.common.unann.UnannPrimitiveTypeTranslate;
import com.test.migration.service.translate.bnf.expression.ExpressionTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;

public class PrimaryNoNewArrayLfNoPrimaryTranslate {
    /**
     * primaryNoNewArray_lfno_primary
     * :	literal
     * |	typeName ('[' ']')* '.' 'class'
     * |	unannPrimitiveType ('[' ']')* '.' 'class'
     * |	'void' '.' 'class'
     * |	'this'
     * |	typeName '.' 'this'
     * |	'(' expression ')'
     * |	classInstanceCreationExpression_lfno_primary
     * |	fieldAccess_lfno_primary
     * |	arrayAccess_lfno_primary
     * |	methodInvocation_lfno_primary
     * |	methodReference_lfno_primary
     * ;
     */
    public String translatePrimaryNoNewArray_lfno_primary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_primaryNoNewArray_lfno_primary) {
            Log.error("RULE_primaryNoNewArray_lfno_primary error");
            return null;
        }
        ParseTree firstTerminalNode = null;
        ParserRuleContext literalCtx = null;
        ParserRuleContext typeNameCtx = null;
        ParserRuleContext unannPrimitiveTypeCtx = null;
        ParserRuleContext expressionCtx = null;
        ParserRuleContext classInstanceCreationExpression_lfno_primaryCtx = null;
        ParserRuleContext fieldAccess_lfno_primaryCtx = null;
        ParserRuleContext arrayAccess_lfno_primaryCtx = null;
        ParserRuleContext methodInvocation_lfno_primaryCtx = null;
        ParserRuleContext methodReference_lfno_primaryCtx = null;
        List<String> leftList = Lists.newArrayList();

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_literal) {
                    literalCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_typeName) {
                    typeNameCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_unannPrimitiveType) {
                    unannPrimitiveTypeCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_expression) {
                    expressionCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_classInstanceCreationExpression_lfno_primary) {
                    classInstanceCreationExpression_lfno_primaryCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_fieldAccess_lfno_primary) {
                    fieldAccess_lfno_primaryCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_arrayAccess_lfno_primary) {
                    arrayAccess_lfno_primaryCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_methodInvocation_lfno_primary) {
                    methodInvocation_lfno_primaryCtx = (ParserRuleContext) child;
                }
                if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_methodReference_lfno_primary) {
                    methodReference_lfno_primaryCtx = (ParserRuleContext) child;
                }
            }
            if (firstTerminalNode == null && child instanceof TerminalNode) {
                firstTerminalNode = child;
            }
            if (child instanceof TerminalNode && StringUtils.equals(child.getText(), "[")) {
                leftList.add("[]");
            }
        }

        LiteralTranslate literalTranslate = new LiteralTranslate();
        TypeNameTranslate typeNameTranslate = new TypeNameTranslate();
        UnannPrimitiveTypeTranslate unannPrimitiveTypeTranslate = new UnannPrimitiveTypeTranslate();
        ExpressionTranslate expressionTranslate = new ExpressionTranslate();
        ClassInstanceCreationExpressionLfnoPrimaryTranslate classInstanceCreationExpressionLfnoPrimaryTranslate = new ClassInstanceCreationExpressionLfnoPrimaryTranslate();
        FieldAccessLfnoPrimaryTranslate fieldAccessLfnoPrimaryTranslate = new FieldAccessLfnoPrimaryTranslate();
        ArrayAccessLfnoPrimaryTranslate arrayAccessLfnoPrimaryTranslate = new ArrayAccessLfnoPrimaryTranslate();
        MethodInvocationLfnoPrimaryTranslate methodInvocationLfnoPrimaryTranslate = new MethodInvocationLfnoPrimaryTranslate();
        MethodReferenceLfnoPrimaryTranslate methodReferenceLfnoPrimaryTranslate = new MethodReferenceLfnoPrimaryTranslate();

        String literal = literalCtx == null ? "" : literalTranslate.translateLiteral(literalCtx);
        String typeName = typeNameCtx == null ? "" : typeNameTranslate.translateTypeName(typeNameCtx);
        String unannPrimitiveType = unannPrimitiveTypeCtx == null ? "" : unannPrimitiveTypeTranslate.translateUnannPrimitiveType(unannPrimitiveTypeCtx);
        String expression = expressionCtx == null ? "" : expressionTranslate.translateExpression(expressionCtx);
        String classInstanceCreationExpressionLfnoPrimary = classInstanceCreationExpression_lfno_primaryCtx == null ? ""
                : classInstanceCreationExpressionLfnoPrimaryTranslate.translateClassInstanceCreationExpression_lfno_primary(classInstanceCreationExpression_lfno_primaryCtx);
        String fieldAccessLfnoPrimary = fieldAccess_lfno_primaryCtx == null ? "" : fieldAccessLfnoPrimaryTranslate.translateFieldAccessLfnoPrimary(fieldAccess_lfno_primaryCtx);
        String arrayAccessLfnoPrimary = arrayAccess_lfno_primaryCtx == null ? "" : arrayAccessLfnoPrimaryTranslate.translateArrayAccess_lfno_primary(arrayAccess_lfno_primaryCtx);
        String methodInvocationLfnoPrimary = methodInvocation_lfno_primaryCtx == null ? "" : methodInvocationLfnoPrimaryTranslate.translateMethodInvocationLfNoPrimary(methodInvocation_lfno_primaryCtx);
        String methodReferenceLfnoPrimary = methodReference_lfno_primaryCtx == null ? "" : methodReferenceLfnoPrimaryTranslate.translateMethodReferenceLfnoPrimary(methodReference_lfno_primaryCtx);
        String leftListStr = Joiner.on("").join(leftList);

        String firstNodeStr = firstTerminalNode == null ? "" : firstTerminalNode.getText();
        ParseTree firstChild = ctx.getChild(0);
        boolean isRuleContext = firstChild instanceof RuleContext;
        if (!isRuleContext) {
            if (StringUtils.equals("void", firstNodeStr)) {
                return "void" + "." + "class";
            }
            if (StringUtils.equals("this", firstNodeStr)) {
                return "this";
            }
            if (StringUtils.equals("(", firstNodeStr)) {
                return " (" + expression + ")";
            }
        } else {
            int ruleIndex = ((RuleContext) firstChild).getRuleIndex();
            // literal
            if (ruleIndex == Java8Parser.RULE_literal) {
                return literal;
            }

            if (ruleIndex == Java8Parser.RULE_typeName) {
                //typeName ('[' ']')* '.' 'class'
                if (ctx.getText().contains(".") && ctx.getText().contains("class")) {
                    return typeName + leftListStr + "." + "class";
                } else {
                    //typeName '.' 'this'
                    return typeName + "." + "this";
                }
            }

            //unannPrimitiveType ('[' ']')* '.' 'class'
            if (ruleIndex == Java8Parser.RULE_unannPrimitiveType) {
                return unannPrimitiveType + leftListStr + "." + "class";
            }

            if (ruleIndex == Java8Parser.RULE_classInstanceCreationExpression_lfno_primary) {
                return classInstanceCreationExpressionLfnoPrimary;
            }

            if (ruleIndex == Java8Parser.RULE_fieldAccess_lfno_primary) {
                return fieldAccessLfnoPrimary;
            }

            if (ruleIndex == Java8Parser.RULE_arrayAccess_lfno_primary) {
                return arrayAccessLfnoPrimary;
            }

            if (ruleIndex == Java8Parser.RULE_methodInvocation_lfno_primary) {
                return methodInvocationLfnoPrimary;
            }

            if (ruleIndex == Java8Parser.RULE_methodReference_lfno_primary) {
                return methodReferenceLfnoPrimary;
            }
        }

        Log.error("translatePrimaryNoNewArray_lfno_primary error");
        return null;
    }
}
