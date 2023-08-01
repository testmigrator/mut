package com.test.migration.service.translate.replace;

import com.test.migration.service.translate.MappingRuleLoader;
import com.test.migration.service.translate.TestCodeContext;
import com.test.migration.service.translate.TranslateHint;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class MethodInvocationLfNoPrimaryReplace {

    /**
     * methodName '(' argumentList? ')'
     *
     * @param originals
     * @return
     */
    public String replaceStructure1(List<String> originals) {
        String methodName = originals.get(0);
        String argumentList = originals.get(2);

        Map<String, String> testToolMethodNameMapping = MappingRuleLoader.testToolApiMapping;
        if (testToolMethodNameMapping.containsKey(methodName)) {
            methodName = testToolMethodNameMapping.get(methodName);
        }

        return methodName + " (" + argumentList + ")";
    }

    /**
     * typeName '.' typeArguments? Identifier '(' argumentList? ')'
     *
     * @param originals
     * @return
     */
    public String replaceStructure2(List<String> originals) {
        String typeName = originals.get(0);
        String typeArguments = originals.get(2);
        String methodNameIdentifier = originals.get(3);
        String argumentList = originals.get(5);

        String newTypeName = typeName;
        String newMethodNameIdentifier = methodNameIdentifier;
        // 如果是类静态方法调用，typeName是类名，methodNameIdentifier是方法名
        MappingRuleLoader.MethodInvocationTypeNameRule staticMethodApi = MappingRuleLoader.apiMapping.stream()
                .filter(x -> StringUtils.equals(x.getSourceClassName(), typeName))
                .filter(x -> StringUtils.equals(x.getSourceMethodName(), methodNameIdentifier))
                .findFirst().orElse(null);
        if (staticMethodApi != null) {
            newTypeName = staticMethodApi.getTargetClassName();
            newMethodNameIdentifier = staticMethodApi.getTargetMethodName();
        } else {
            newMethodNameIdentifier = MethodInvocationReplace.replaceInvocationMethodName(typeName, methodNameIdentifier);
        }

        return newTypeName + "->" + typeArguments + newMethodNameIdentifier + " (" + argumentList + ")";
    }

    /**
     * expressionName '.' typeArguments? Identifier '(' argumentList? ')'
     *
     * @param originals
     * @return
     */
    public String replaceStructure3(List<String> originals) {
        String expressionName = originals.get(0);
        String typeArguments = originals.get(2);
        String identifier = originals.get(3);
        String argumentList = originals.get(5);

        String newStructure = expressionName + "->" + typeArguments + identifier + " (" + argumentList + ")";
        return newStructure;
    }

    /**
     * 'super' '.' typeArguments? Identifier '(' argumentList? ')'
     *
     * @param originals
     * @return
     */
    public String replaceStructure4(List<String> originals) {
        String typeArguments = originals.get(2);
        String identifierStr = originals.get(3);
        String argumentList = originals.get(5);

        return "super" + "->" + typeArguments + identifierStr + " (" + argumentList + ")";
    }

    /**
     * typeName '.' 'super' '.' typeArguments? Identifier '(' argumentList? ')'
     *
     * @param originals
     * @return
     */
    public String replaceStructure5(List<String> originals) {
        String typeName = originals.get(0);
        String typeArguments = originals.get(4);
        String methodNameIdentifier = originals.get(5);
        String argumentList = originals.get(7);

        String newStructure = typeName + "->" + "super" + "->" + typeArguments + " " + methodNameIdentifier + " (" + argumentList + ")";

        return newStructure;
    }


}
