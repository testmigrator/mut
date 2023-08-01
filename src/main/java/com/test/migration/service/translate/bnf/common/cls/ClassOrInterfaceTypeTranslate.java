package com.test.migration.service.translate.bnf.common.cls;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class ClassOrInterfaceTypeTranslate {

    /**
     * classOrInterfaceType
     * :	(	classType_lfno_classOrInterfaceType
     * |	interfaceType_lfno_classOrInterfaceType
     * )
     * (	classType_lf_classOrInterfaceType
     * |	interfaceType_lf_classOrInterfaceType
     * )*
     * ;
     */
    public String translateClassOrInterfaceType(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_classOrInterfaceType)) {
            Log.error("RULE_classOrInterfaceType error");
            return null;
        }

        ParserRuleContext classType_lfno_classOrInterfaceTypeCtx = null;
        ParserRuleContext interfaceType_lfno_classOrInterfaceTypeCtx = null;
        List<ParserRuleContext> classType_lf_classOrInterfaceTypeCtxList = Lists.newArrayList();
        List<ParserRuleContext> interfaceType_lf_classOrInterfaceTypeCtxList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_classType_lfno_classOrInterfaceType) {
                classType_lfno_classOrInterfaceTypeCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_interfaceType_lfno_classOrInterfaceType) {
                interfaceType_lfno_classOrInterfaceTypeCtx = (ParserRuleContext) childRuleContext;
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_classType_lf_classOrInterfaceType) {
                classType_lf_classOrInterfaceTypeCtxList.add((ParserRuleContext) childRuleContext);
            }
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_interfaceType_lf_classOrInterfaceType) {
                interfaceType_lf_classOrInterfaceTypeCtxList.add((ParserRuleContext) childRuleContext);
            }
        }

        ClassTypeLfnoClassOrInterfaceTypeTranslate classTypeLfnoClassOrInterfaceTypeTranslate = new ClassTypeLfnoClassOrInterfaceTypeTranslate();
        String classType_lfno_classOrInterfaceType = classType_lfno_classOrInterfaceTypeCtx == null ? ""
                : classTypeLfnoClassOrInterfaceTypeTranslate.translateClassType_lfno_classOrInterfaceType(classType_lfno_classOrInterfaceTypeCtx);

        InterfaceTypeLfnoClassOrInterfaceTypeTranslate interfaceTypeLfnoClassOrInterfaceTypeTranslate = new InterfaceTypeLfnoClassOrInterfaceTypeTranslate();
        String interfaceType_lfno_classOrInterfaceType = interfaceType_lfno_classOrInterfaceTypeCtx == null ? ""
                : interfaceTypeLfnoClassOrInterfaceTypeTranslate.translateInterfaceType_lfno_classOrInterfaceType(interfaceType_lfno_classOrInterfaceTypeCtx);

        ClassTypeLfClassOrInterfaceTypeTranslate classTypeLfClassOrInterfaceTypeTranslate = new ClassTypeLfClassOrInterfaceTypeTranslate();
        List<String> classTypeLfClassOrInterfaceTypeList = classType_lf_classOrInterfaceTypeCtxList.stream()
                .map(classTypeLfClassOrInterfaceTypeTranslate::translateClassType_lf_classOrInterfaceType)
                .collect(Collectors.toList());
        String classTypeLfClassOrInterfaceTypeStr = Joiner.on(" ").join(classTypeLfClassOrInterfaceTypeList);

        InterfaceTypeLfClassOrInterfaceTypeTranslate interfaceTypeLfClassOrInterfaceTypeTranslate = new InterfaceTypeLfClassOrInterfaceTypeTranslate();
        List<String> interfaceTypeLfClassOrInterfaceTypeList = interfaceType_lf_classOrInterfaceTypeCtxList.stream()
                .map(interfaceTypeLfClassOrInterfaceTypeTranslate::translateInterfaceType_lf_classOrInterfaceType)
                .collect(Collectors.toList());
        String interfaceTypeLfClassOrInterfaceTypeStr = Joiner.on(" ").join(interfaceTypeLfClassOrInterfaceTypeList);

        return classType_lfno_classOrInterfaceType + " " + interfaceType_lfno_classOrInterfaceType +
                " " + classTypeLfClassOrInterfaceTypeStr + " " + interfaceTypeLfClassOrInterfaceTypeStr;
    }
}
