package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class TypeParametersTranslate {

    /**
     * typeParameters
     * :	'<' typeParameterList '>'
     * ;
     */
    public String translateTypeParameters(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_typeParameters)) {
            Log.error("RULE_typeParameters error");
            return null;
        }

        ParserRuleContext childRuleContext = (ParserRuleContext) ctx.getChild(1);
        TypeParameterListTranslate translate = new TypeParameterListTranslate();
        String typeParameterList = translate.translateTypeParameterList(childRuleContext);

        return "<" + typeParameterList + ">";
    }
}
