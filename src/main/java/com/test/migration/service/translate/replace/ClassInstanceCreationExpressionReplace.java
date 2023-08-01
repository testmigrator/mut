package com.test.migration.service.translate.replace;

import com.test.migration.service.translate.MappingRuleLoader;
import com.test.migration.service.translate.TestCodeContext;
import com.test.migration.service.translate.TranslateHint;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class ClassInstanceCreationExpressionReplace {

    /**
     * 'new' Identifier '(' argumentList? ')' classBody?
     */
    public String replaceStructure1(List<String> original) {
        String identifier = original.get(1);
        String argumentList = original.get(3);
        String classBody = original.get(5);
        identifier = replaceClassInstanceCreationIdentifier(identifier);

        String newStructure = "new " + identifier + " (" + argumentList + ")" + classBody;
        return newStructure;
    }


    /**
     * expressionName '.' 'new' Identifier '(' argumentList? ')' classBody?
     */
    public String replaceStructure2(List<String> original) {
        String expressionName = original.get(0);
        String identifier = original.get(3);
        String argumentList = original.get(5);
        String classBody = original.get(7);
        identifier = replaceClassInstanceCreationIdentifier(identifier);

        String newStructure = expressionName + "->" + "new " + identifier + " (" + argumentList + ")" + classBody;
        return newStructure;
    }

    /**
     * primary '.' 'new' Identifier '(' argumentList? ')' classBody?
     */
    public String replaceStructure3(List<String> original) {
        String expressionName = original.get(0);
        String identifier = original.get(3);
        String argumentList = original.get(5);
        String classBody = original.get(7);
        identifier = replaceClassInstanceCreationIdentifier(identifier);

        String newStructure = expressionName + "->" + "new " + identifier + " (" + argumentList + ")" + classBody;
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
