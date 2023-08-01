package com.test.migration.service.translate.bnf.common.unann;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class UnannClassOrInterfaceTypeTranslate {

    /**
     * unannClassOrInterfaceType
     * :	(	unannClassType_lfno_unannClassOrInterfaceType
     * |	unannInterfaceType_lfno_unannClassOrInterfaceType
     * )
     * (	unannClassType_lf_unannClassOrInterfaceType
     * |	unannInterfaceType_lf_unannClassOrInterfaceType
     * )*
     * ;

     */
    public String translateUnannClassOrInterfaceType(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_unannClassOrInterfaceType) {
            Log.error("RULE_unannClassOrInterfaceType error");
            return null;
        }
        ParserRuleContext unannClassType_lfno_unannClassOrInterfaceTypeCtx = null;
        ParserRuleContext unannInterfaceType_lfno_unannClassOrInterfaceTypeCtx = null;
        List<ParserRuleContext> unannClassType_lf_unannClassOrInterfaceTypeCtxList = Lists.newArrayList();
        List<ParserRuleContext> unannInterfaceType_lf_unannClassOrInterfaceTypeCtxList = Lists.newArrayList();

        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            ParserRuleContext ctxChild = (ParserRuleContext) ctx.getChild(i);
            if (ctxChild.getRuleIndex() == Java8Parser.RULE_unannClassType_lfno_unannClassOrInterfaceType) {
                unannClassType_lfno_unannClassOrInterfaceTypeCtx = ctxChild;
            }
            if (ctxChild.getRuleIndex() == Java8Parser.RULE_unannInterfaceType_lfno_unannClassOrInterfaceType) {
                unannInterfaceType_lfno_unannClassOrInterfaceTypeCtx = ctxChild;
            }
            if (ctxChild.getRuleIndex() == Java8Parser.RULE_unannClassType_lf_unannClassOrInterfaceType) {
                unannClassType_lf_unannClassOrInterfaceTypeCtxList.add(ctxChild);
            }
            if (ctxChild.getRuleIndex() == Java8Parser.RULE_unannInterfaceType_lf_unannClassOrInterfaceType) {
                unannInterfaceType_lf_unannClassOrInterfaceTypeCtxList.add(ctxChild);
            }
        }

        UnannClassTypeLfnoUnannClassOrInterfaceTypeTranslate unannClassTypeLfnoUnannClassOrInterfaceTypeTranslate =
                new UnannClassTypeLfnoUnannClassOrInterfaceTypeTranslate();

        UnannInterfaceTypeLfnoUnannClassOrInterfaceTypeTranslate unannInterfaceTypeLfnoUnannClassOrInterfaceTypeTranslate =
                new UnannInterfaceTypeLfnoUnannClassOrInterfaceTypeTranslate();

        UnannClassTypeLfUnannClassOrInterfaceTypeTranslate unannClassTypeLfUnannClassOrInterfaceTypeTranslate =
                new UnannClassTypeLfUnannClassOrInterfaceTypeTranslate();

        UnannInterfaceTypeLfUnannClassOrInterfaceTypeTranslate unannInterfaceTypeLfUnannClassOrInterfaceTypeTranslate =
                new UnannInterfaceTypeLfUnannClassOrInterfaceTypeTranslate();

        String unannClassType_lfno_unannClassOrInterfaceType = unannClassType_lfno_unannClassOrInterfaceTypeCtx == null ? ""
                : unannClassTypeLfnoUnannClassOrInterfaceTypeTranslate.translateUnannClassTypeLfnoUnannClassOrInterfaceType(unannClassType_lfno_unannClassOrInterfaceTypeCtx);

        String unannInterfaceType_lfno_unannClassOrInterfaceType = unannInterfaceType_lfno_unannClassOrInterfaceTypeCtx == null ? ""
                : unannInterfaceTypeLfnoUnannClassOrInterfaceTypeTranslate.translateUnannInterfaceTypeLfnoUnannClassOrInterfaceType(unannInterfaceType_lfno_unannClassOrInterfaceTypeCtx);

        List<String> unannClassType_lf_unannClassOrInterfaceTypeList = unannClassType_lf_unannClassOrInterfaceTypeCtxList.stream()
                .map(unannClassTypeLfUnannClassOrInterfaceTypeTranslate::translateUnannClassTypeLfUnannClassOrInterfaceType)
                .collect(Collectors.toList());
        String unannClassType_lf_unannClassOrInterfaceType = Joiner.on(" ").join(unannClassType_lf_unannClassOrInterfaceTypeList);

        List<String> unannInterfaceType_lf_unannClassOrInterfaceTypeList = unannInterfaceType_lf_unannClassOrInterfaceTypeCtxList.stream()
                .map(unannInterfaceTypeLfUnannClassOrInterfaceTypeTranslate::translateUnannInterfaceTypeLfUnannClassOrInterfaceType)
                .collect(Collectors.toList());
        String unannInterfaceType_lf_unannClassOrInterfaceType = Joiner.on(" ").join(unannInterfaceType_lf_unannClassOrInterfaceTypeList);


        return unannClassType_lfno_unannClassOrInterfaceType + " "
                + unannInterfaceType_lfno_unannClassOrInterfaceType + " "
                + unannClassType_lf_unannClassOrInterfaceType + " "
                + unannInterfaceType_lf_unannClassOrInterfaceType;
    }
}
