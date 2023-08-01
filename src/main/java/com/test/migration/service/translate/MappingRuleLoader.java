package com.test.migration.service.translate;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import utils.MappingRuleReader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Data
public class MappingRuleLoader {

    public static Map<String, String> classNameMapping;
    public static List<MethodInvocationTypeNameRule> apiMapping;
    public static Map<String, String> testToolApiMapping;


    public static void load() {
        List<String> apiMappingStr = MappingRuleReader.readLinesFromProperties("mappingRule/apiMapping.properties");
        apiMapping = apiMappingStr.stream()
                .map(x->x.replace("\\:",":"))
                .map(MappingRuleLoader::buildMapping).collect(Collectors.toList());

        List<String> testToolApiMappingStr = MappingRuleReader.readLinesFromProperties("mappingRule/testToolApiMapping.properties");
        testToolApiMapping = convertToMap(testToolApiMappingStr);

        List<String> classNameMappingStr = MappingRuleReader.readLinesFromProperties("mappingRule/classNameMapping.properties");
        classNameMapping = convertToMap(classNameMappingStr);
    }

    private static Map<String, String> convertToMap(List<String> mappingStr) {
        Map<String, String> map = Maps.newHashMap();
        for (String line : mappingStr) {
            String[] mapping = line.split("=");
            if (mapping.length < 2) {
                map.put(mapping[0], StringUtils.EMPTY);
            } else {
                map.put(mapping[0], mapping[1]);
            }
        }
        return map;
    }

    private static MethodInvocationTypeNameRule buildMapping(String mappingLine) {
        String[] mappingStr = mappingLine.split("=");
        String sourceStr = mappingStr[0];
        String targetStr = StringUtils.EMPTY;

        MethodInvocationTypeNameRule rule = new MethodInvocationTypeNameRule();
        if (mappingStr.length < 2) {
            rule.setTargetClassName(StringUtils.EMPTY);
            rule.setTargetMethodName(StringUtils.EMPTY);
        } else {
            targetStr = mappingStr[1];
        }

        if (StringUtils.isNotBlank(sourceStr)) {
            String[] source = sourceStr.split("->");
            rule.setSourceClassName(source[0]);
            rule.setSourceMethodName(source[1]);
        } else {
            rule.setSourceClassName(StringUtils.EMPTY);
            rule.setSourceMethodName(StringUtils.EMPTY);
        }

        if (StringUtils.isNotBlank(targetStr)) {
            String[] target = targetStr.split("->");
            rule.setTargetClassName(target[0]);
            rule.setTargetMethodName(target[1]);
        } else {
            rule.setTargetClassName(StringUtils.EMPTY);
            rule.setTargetMethodName(StringUtils.EMPTY);
        }

        return rule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodInvocationTypeNameRule {
        private String sourceClassName;
        private String sourceMethodName;
        private String targetClassName;
        private String targetMethodName;
    }

}
