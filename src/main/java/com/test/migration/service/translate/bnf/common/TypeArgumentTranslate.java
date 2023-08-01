package com.test.migration.service.translate.bnf.common;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;


public class TypeArgumentTranslate {

    /**
     * typeArgument
     * :	referenceType
     * |	wildcard
     * ;
     */
    public String translateTypeArgument(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_typeArgument)) {
            Log.error("RULE_typeArgument error");
            return null;
        }
        ParserRuleContext child = (ParserRuleContext) ctx.getChild(0);
        if (child.getRuleIndex() == Java8Parser.RULE_referenceType) {
            ReferenceTypeTranslate referenceTypeTranslate = new ReferenceTypeTranslate();
            return referenceTypeTranslate.translateReferenceType(child);
        }

        if (child.getRuleIndex() == Java8Parser.RULE_wildcard) {
            WildcardTranslate wildcardTranslate = new WildcardTranslate();
            return wildcardTranslate.translateWildCard(child);
        }
        Log.error("translateTypeArgument error");

        return null;
    }
}
