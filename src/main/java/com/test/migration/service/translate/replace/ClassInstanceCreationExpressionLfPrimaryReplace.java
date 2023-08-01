package com.test.migration.service.translate.replace;

import com.test.migration.service.translate.MappingRuleLoader;
import com.test.migration.service.translate.TestCodeContext;
import com.test.migration.service.translate.TranslateHint;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;
import java.util.Map;

public class ClassInstanceCreationExpressionLfPrimaryReplace {
    /**
     * '.' 'new' Identifier '(' argumentList? ')' classBody?
     */
    public String replaceStructure1(List<String> original) {
        String identifier = original.get(2);
        String argumentList = original.get(4);
        String classBody = original.get(6);
        identifier = replaceClassInstanceCreationIdentifier(identifier);

        String newStructure = "->" + "new " + identifier + " (" + argumentList + ")" + classBody;
        return newStructure;
    }

    public static String replaceClassInstanceCreationIdentifier(String identifier) {
        Map<String, String> commonClassNameMapping = MappingRuleLoader.classNameMapping;

        // 内部类 改为A::B
        boolean isInnerCls = TestCodeContext.ClassMemberDeclaration.classes.stream()
                .anyMatch(x -> StringUtils.equals(x.name, identifier));
        if (isInnerCls) {
            return TestCodeContext.className + "::" + identifier;
        }

        // hint for mismatch
        if (!commonClassNameMapping.containsKey(identifier)) {
            TranslateHint.misMatchCodes.add(identifier);
        }

        return commonClassNameMapping.getOrDefault(identifier, identifier);
    }
}
