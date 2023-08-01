package com.test.migration.service.translate.bnf.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class TypeParameterListTranslate {

    /**
     * typeParameterList
     * :	typeParameter (',' typeParameter)*
     * ;
     *
     * @param ctx
     * @return
     */
    public String translateTypeParameterList(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_typeParameterList)) {
            Log.error("RULE_typeParameterList error");
            return null;
        }
        List<ParserRuleContext> typeParameterCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof RuleContext) {
                if (((RuleContext) ctx.getChild(i)).getRuleIndex() == Java8Parser.RULE_typeParameter) {
                    typeParameterCtxList.add((ParserRuleContext) ctx.getChild(i));
                }
            }
        }

        TypeParameterTranslate typeParameterTranslate = new TypeParameterTranslate();
        List<String> list = typeParameterCtxList.stream()
                .map(typeParameterTranslate::translateTypeParameter)
                .collect(Collectors.toList());

        String typeParameterList = Joiner.on(",").join(list);

        return "<" + typeParameterList + ">";
    }
}
