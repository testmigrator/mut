package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.array.ArrayTypeTranslate;
import com.test.migration.service.translate.bnf.common.cls.ClassOrInterfaceTypeTranslate;
import com.test.migration.service.translate.bnf.common.variable.TypeVariableTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class ReferenceTypeTranslate {

    /**
     * referenceType
     * :	classOrInterfaceType
     * |	typeVariable
     * |	arrayType
     * ;
     */
    public String translateReferenceType(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_referenceType)) {
            Log.error("RULE_referenceType error" );
            return null;
        }

        ParserRuleContext child = (ParserRuleContext) ctx.getChild(0);

        if (child.getRuleIndex() == Java8Parser.RULE_classOrInterfaceType) {
            ClassOrInterfaceTypeTranslate classOrInterfaceTypeTranslate = new ClassOrInterfaceTypeTranslate();
            return classOrInterfaceTypeTranslate.translateClassOrInterfaceType(child);
        }

        if (child.getRuleIndex() == Java8Parser.RULE_typeVariable) {
            TypeVariableTranslate typeVariableTranslate = new TypeVariableTranslate();
            return typeVariableTranslate.translateTypeVariable(child);
        }

        if (child.getRuleIndex() == Java8Parser.RULE_arrayType) {
            ArrayTypeTranslate arrayTypeTranslate = new ArrayTypeTranslate();
            return arrayTypeTranslate.translateArrayType(child);
        }

        Log.error("translateReferenceType error");

        return null;
    }
}
