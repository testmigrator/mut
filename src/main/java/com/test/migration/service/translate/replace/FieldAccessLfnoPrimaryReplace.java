package com.test.migration.service.translate.replace;

import com.test.migration.service.translate.MappingRuleLoader;
import com.test.migration.service.translate.TestCodeContext;
import com.test.migration.service.translate.TranslateHint;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class FieldAccessLfnoPrimaryReplace {
    /**
     * typeName '.' 'super' '.' Identifier
     */
    public String replaceStructure2(List<String> originals) {
        String typeName = originals.get(0);
        String identifier = originals.get(4);
        String newTypeName = replaceClassOrInterfaceType(typeName);
        String newStructure = newTypeName + "->" +"super"+"->"+ identifier;
        return newStructure;
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
