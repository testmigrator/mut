package com.test.migration.service.translate.bnf.common.primary;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.array.ArrayAccessLfPrimaryTranslate;
import com.test.migration.service.translate.bnf.common.cls.ClassInstanceCreationExpressionLfPrimaryTranslate;
import com.test.migration.service.translate.bnf.common.method.MethodInvocationLfPrimaryTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class PrimaryNoNewArrayLfPrimaryTranslate {

    /**
     * primaryNoNewArray_lf_primary
     * 	:	classInstanceCreationExpression_lf_primary
     * 	|	fieldAccess_lf_primary
     * 	|	arrayAccess_lf_primary
     * 	|	methodInvocation_lf_primary
     * 	|	methodReference_lf_primary
     * 	;
     */
    public String translatePrimaryNoNewArray_lf_primary(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_primaryNoNewArray_lf_primary) {
            Log.error("RULE_primaryNoNewArray_lf_primary error");
            return null;
        }

        ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(0);

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_classInstanceCreationExpression_lf_primary) {
            ClassInstanceCreationExpressionLfPrimaryTranslate translate = new ClassInstanceCreationExpressionLfPrimaryTranslate();
            return translate.translateClassInstanceCreationExpression_lf_primary(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_fieldAccess_lf_primary) {
            FieldAccessLfPrimaryTranslate translate = new FieldAccessLfPrimaryTranslate();
            return translate.translateFieldAccessLfPrimary(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_arrayAccess_lf_primary) {
            ArrayAccessLfPrimaryTranslate translate = new ArrayAccessLfPrimaryTranslate();
            return translate.translateArrayAccessLfPrimary(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_methodInvocation_lf_primary) {
            MethodInvocationLfPrimaryTranslate translate = new MethodInvocationLfPrimaryTranslate();
            return translate.translateMethodInvocationLfPrimary(childRuleContext);
        }

        if (childRuleContext.getRuleIndex() == Java8Parser.RULE_methodReference_lf_primary) {
            MethodReferenceLfPrimaryTranslate translate = new MethodReferenceLfPrimaryTranslate();
            return translate.translateMethodReferenceLfPrimary(childRuleContext);
        }

        return null;
    }

}
