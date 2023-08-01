package com.test.migration.service.translate.bnf.common.cls;

import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.bnf.declaration.ClassBodyDeclarationTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import utils.Log;

public class ClassBodyTranslate {

    /**
     * classBody
     * :	'{' classBodyDeclaration* '}'
     * ;
     */
    public String translateClassBody(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_classBody)) {
            Log.info("RULE_classBody error");
            return null;
        }

        ClassBodyDeclarationTranslate classBodyDeclarationTranslate = new ClassBodyDeclarationTranslate();
        StringBuilder classBodyDeclarations = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext node = (RuleContext) child;

            if (node.getRuleIndex() == Java8Parser.RULE_classBodyDeclaration) {
                String classBodyDeclaration = classBodyDeclarationTranslate.translateClassBodyDeclaration((ParserRuleContext) node);
                classBodyDeclarations.append(classBodyDeclaration);
            }
        }

        return " { " + classBodyDeclarations + " } ";
    }
}
