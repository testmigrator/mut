package com.test.migration.service.translate.replace;

import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.translate.MappingRuleLoader;
import com.test.migration.service.translate.TestCodeContext;
import com.test.migration.service.translate.TranslateHint;
import com.test.migration.service.translate.bnf.common.TypeArgumentsTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;
import java.util.Map;

public class UnannClassTypeLfnoUnannClassOrInterfaceTypeReplace {

    /**
     * Identifier typeArguments?
     *
     * @param originals
     * @return
     */
    public String replaceStructure1(List<String> originals) {
        String identifier = originals.get(0);
        String typeArguments = originals.get(1);
        identifier = replaceClassOrInterfaceType(identifier);

        return identifier + " " + typeArguments;
    }

    public String replaceClassOrInterfaceType(String clsName) {
        Map<String, String> referenceMapping = MappingRuleLoader.classNameMapping;
        if (StringUtils.equals("String", clsName)) {
            return referenceMapping.get(clsName);
        }

        // 内部类 改为A::B
        boolean isInnerCls = TestCodeContext.ClassMemberDeclaration.classes.stream()
                .anyMatch(x -> StringUtils.equals(x.name, clsName));
        if (isInnerCls) {
            return TestCodeContext.className + "::" + clsName;
        }

        // hint for mismatch
        if (!referenceMapping.containsKey(clsName)) {
            TranslateHint.misMatchCodes.add(clsName);
        }

        return referenceMapping.getOrDefault(clsName, clsName) + " * ";
    }
}
