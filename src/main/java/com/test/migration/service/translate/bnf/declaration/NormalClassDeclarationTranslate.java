package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.cls.ClassBodyTranslate;
import com.test.migration.service.translate.bnf.common.TypeParametersTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

public class NormalClassDeclarationTranslate {

    /**
     * normalClassDeclaration
     * :	classModifier* 'class' Identifier typeParameters? superclass? superinterfaces? classBody
     * ;
     * TIPS:
     * classModifier，superclass，superinterfaces均不在转换
     */
    public String translateNormalClassDeclaration(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_normalClassDeclaration)) {
            Log.error("RULE_normalClassDeclaration error");
            return null;
        }

        // Identifier
        String identifier = "";

        ParserRuleContext typeParametersRule = null;
        ParserRuleContext classBodyRule = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            // todo trick
            if (StringUtils.equals(ctx.getChild(i).getText(), "class")) {
                identifier = ctx.getChild(i + 1).getText();
            }

            if (ctx.getChild(i) instanceof RuleContext &&
                    ((RuleContext) ctx.getChild(i)).getRuleIndex() == Java8Parser.RULE_typeParameters) {
                typeParametersRule = (ParserRuleContext) ctx.getChild(i);
            }

            if (ctx.getChild(i) instanceof RuleContext &&
                    ((RuleContext) ctx.getChild(i)).getRuleIndex() == Java8Parser.RULE_classBody) {
                classBodyRule = (ParserRuleContext) ctx.getChild(i);
            }
        }

        // typeParameters 范型
        String typeParameters = StringUtils.EMPTY;
        if (typeParametersRule != null) {
            TypeParametersTranslate typeParametersTranslate = new TypeParametersTranslate();
            typeParameters = typeParametersTranslate.translateTypeParameters(typeParametersRule);
        }

        // classBody
        ClassBodyTranslate classBodyTranslate = new ClassBodyTranslate();
        String classBody = classBodyTranslate.translateClassBody(classBodyRule);

        return identifier + " " + typeParameters + " " + classBody;
    }
}
