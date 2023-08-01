package com.test.migration.service.translate.replace;

import com.test.migration.service.translate.MappingRuleLoader;
import com.test.migration.service.translate.TestCodeContext;
import com.test.migration.service.translate.TranslateHint;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;
import java.util.Map;

public class SimpleTypeNameReplace {

    /**
     * Identifier
     */
    public String replaceStructure1(List<String> originals) {
        String identifier = originals.get(0);
        return replaceClassName(identifier);
    }

    public static String replaceClassName(String identifier) {
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
