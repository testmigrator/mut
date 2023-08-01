package com.test.migration.service.translate.bnf.common.constructor;

import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.replace.SimpleTypeNameReplace;
import org.antlr.v4.runtime.ParserRuleContext;
import utils.Log;

public class SimpleTypeNameTranslate {

    /**
     * simpleTypeName
     * :	Identifier
     * ;
     */
    public String translateSimpleTypeName(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_simpleTypeName)) {
            Log.error("RULE_simpleTypeName error");
            return null;
        }

        SimpleTypeNameReplace simpleTypeNameReplace = new SimpleTypeNameReplace();
        return simpleTypeNameReplace.replaceStructure1(Lists.newArrayList(ctx.getText()));
    }
}
