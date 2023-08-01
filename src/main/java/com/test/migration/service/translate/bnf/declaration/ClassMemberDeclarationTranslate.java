package com.test.migration.service.translate.bnf.declaration;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.common.method.MethodDeclarationTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class ClassMemberDeclarationTranslate {

    /**
     * classMemberDeclaration
     * :	fieldDeclaration
     * |	methodDeclaration
     * |	classDeclaration
     * |	interfaceDeclaration
     * |	';'
     * ;
     */
    public String translateClassMemberDeclaration(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_classMemberDeclaration)) {
            Log.error("RULE_classMemberDeclaration error");
            return null;
        }

        ParseTree child = ctx.getChild(0);
        boolean isRuleContext = child instanceof RuleContext;
        // ;
        if (!isRuleContext) {
            return ctx.getText();
        }
        ParserRuleContext node = (ParserRuleContext) child;

        //fieldDeclaration
        if (node.getRuleIndex() == Java8Parser.RULE_fieldDeclaration) {
            FieldDeclarationTranslate subTranslate = new FieldDeclarationTranslate();
            return subTranslate.translateFieldDeclaration(node);
        }

        //methodDeclaration
        if (node.getRuleIndex() == Java8Parser.RULE_methodDeclaration) {
            MethodDeclarationTranslate subTranslate = new MethodDeclarationTranslate();
            return subTranslate.translateMethodDeclaration(node);
        }

        //classDeclaration
        if (node.getRuleIndex() == Java8Parser.RULE_classDeclaration) {
            ClassDeclarationTranslate subTranslate = new ClassDeclarationTranslate();
            return subTranslate.translateClassDeclaration(node);
        }

        //interfaceDeclaration
        if (node.getRuleIndex() == Java8Parser.RULE_interfaceDeclaration) {
            InterfaceDeclarationTranslate subTranslate = new InterfaceDeclarationTranslate();
            return subTranslate.translateInterfaceDeclaration(node);
        }

        Log.error("translateClassMemberDeclaration error");

        return null;
    }
}
