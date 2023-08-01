package com.test.migration.service.translate.replace;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.test.migration.entity.Constants;
import com.test.migration.service.translate.MappingRuleLoader;
import com.test.migration.service.translate.TranslateCodeCollector;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClassMappingReplace {
    /**
     * {
     * int a = 0;
     * T1$T2$S1 s = new T1$T2$S1();
     * T3$T4$S2 s2 = new T3$T4$S2();
     * }
     * =>
     * {
     * int a = 0;
     * T1 s = new T1();
     * T4 s2 = new T4();
     * }
     */
    public List<String> replaceClassMappingIfNeed(List<TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode>
                                                           blockStatementTranslateCodes) {
        // 翻译的blockStatement中存在ClassMappingOneToManySplitter分隔符，说明遇到了一对多类匹配关系
        List<TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode> translateCodeList = blockStatementTranslateCodes.stream()
                .filter(x -> x.translateCode.contains(Constants.ClassMappingOneToManySplitter))
                .collect(Collectors.toList());
        List<String> classMappingTips = Lists.newArrayList();

        // 如果不存在多对一类匹配 不用处理
        if (translateCodeList.size() == 0) {
            return Lists.newArrayList();
        }

        // 获取翻译后代码
        List<String> translateCodeStrList = blockStatementTranslateCodes.stream()
                .map(TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode::getTranslateCode)
                .collect(Collectors.toList());

        // 挨个处理 这里对入参做了修改（lowB设计，下不为例）
        for (TranslateCodeCollector.MethodTranslateCode.BlockStatementTranslateCode translateCode : blockStatementTranslateCodes) {
            String bsCode = translateCode.translateCode;
            // 获取一对多类匹配代码
            String classMapping = Splitter.on(" ").splitToList(bsCode).stream()
                    .filter(x -> x.contains(Constants.ClassMappingOneToManySplitter))
                    .findFirst().orElse(null);
            if (StringUtils.isBlank(classMapping)) {
                continue;
            }
            // 拆解出每个class（比如：A-B-C -> A,B,C）
            List<String> classMappingList = Splitter.on(Constants.ClassMappingOneToManySplitter).splitToList(classMapping);
            // 预设规则：最后一个元素是sourceClass，其余是匹配到的targetClass（破规则，下不为例）
            String sourceClass = classMappingList.get(classMappingList.size() - 1);
            List<String> targetClasses = classMappingList.subList(0, classMappingList.size() - 1);

            // 这里根据待迁移代码中的api调用关系，从所有api mapping中进行筛选
            List<MappingRuleLoader.MethodInvocationTypeNameRule> targetClassList = MappingRuleLoader.apiMapping.stream()
                    .filter(apiMapping -> StringUtils.equals(apiMapping.getSourceClassName(), sourceClass))
                    .filter(apiMapping -> targetClasses.contains(apiMapping.getTargetClassName()))
                    .filter(apiMapping -> isExistTargetApiInvocation(translateCodeStrList, apiMapping.getTargetMethodName()))
                    .collect(Collectors.toList());

            Set<String> allTargetClassSet = Sets.newHashSet();
            for (MappingRuleLoader.MethodInvocationTypeNameRule mapping : targetClassList) {
                allTargetClassSet.add(mapping.getTargetClassName());
            }

            // 如果只有一个targetClass，直接source->target class替换，否则给出hint
            String newBsCode = "";
            if (allTargetClassSet.size() == 1) {
                String target = allTargetClassSet.stream().findFirst().orElse("");
                newBsCode = bsCode.replaceAll(Pattern.quote(classMapping), target);
                translateCode.translateCode = newBsCode;
            } else {
                //给出hint
                String newCode = translateCode.translateCode.replaceAll(Pattern.quote(classMapping), sourceClass);
                String multipleClassMapping = Joiner.on(",").join(targetClasses.stream().distinct().collect(Collectors.toList()));
                translateCode.translateCode = newCode;
                String hint = Constants.Hint.multipleClassMappingMessage + "(" + sourceClass + "):" + multipleClassMapping + "\n";
                classMappingTips.add(hint);
            }
        }

        return classMappingTips;

    }

    public String replaceClassMappingIfNeed(TranslateCodeCollector.MethodTranslateCode.MethodHeaderTranslateCode translateCode) {
        String code = translateCode.translateCode;
        // 如果不存在类匹配一对多关系，不处理
        if (!code.contains(Constants.ClassMappingOneToManySplitter)) {
            return "";
        }

        // 拆解出每个class（比如：A-B-C -> A,B,C）
        String classMapping = Splitter.on(" ").splitToList(code).stream()
                .filter(x -> x.contains(Constants.ClassMappingOneToManySplitter))
                .findFirst().orElse(null);
        if (StringUtils.isBlank(classMapping)) {
            return "";
        }
        List<String> classMappingList = Splitter.on(Constants.ClassMappingOneToManySplitter).splitToList(classMapping);
        // 规则：最后一个元素是sourceClass，需要舍弃掉
        List<String> targetClasses = classMappingList.subList(0, classMappingList.size() - 1);
        String sourceClass = classMappingList.get(classMappingList.size() - 1);
        targetClasses = targetClasses.stream().distinct().collect(Collectors.toList());
        String multipleMappingStr = Joiner.on(",").join(targetClasses);
        // 替换code
        translateCode.translateCode = code.replaceAll(Pattern.quote(classMapping), sourceClass);
        return Constants.Hint.multipleClassMappingMessage + "(" + sourceClass + "):" + multipleMappingStr + "\n";
    }


    /**
     * T1$T2$S1 s = new T1$T2$S1();
     * =>
     * T1 s = new T1();
     * T2 s = new T2();
     *
     * @param translateCode
     */
    public String replaceClassMappingIfNeed(TranslateCodeCollector.TranslateCode translateCode) {
        String code = translateCode.translateCode;
        // 如果不存在类匹配一对多关系，不处理
        if (!code.contains(Constants.ClassMappingOneToManySplitter)) {
            return "";
        }

        // 拆解出每个class（比如：A-B-C -> A,B,C）
        String classMapping = Splitter.on(" ").splitToList(code).stream()
                .filter(x -> x.contains(Constants.ClassMappingOneToManySplitter))
                .findFirst().orElse(null);
        if (StringUtils.isBlank(classMapping)) {
            return "";
        }
        List<String> classMappingList = Splitter.on(Constants.ClassMappingOneToManySplitter).splitToList(classMapping);
        // 规则：最后一个元素是sourceClass，需要舍弃掉
        List<String> targetClasses = classMappingList.subList(0, classMappingList.size() - 1);
        String sourceClass = classMappingList.get(classMappingList.size() - 1);
        targetClasses = targetClasses.stream().distinct().collect(Collectors.toList());
        String multipleMappingStr = Joiner.on(",").join(targetClasses);
        // 替换code
        translateCode.translateCode = code.replaceAll(Pattern.quote(classMapping), sourceClass);
        return Constants.Hint.multipleClassMappingMessage + "(" + sourceClass + "):" + multipleMappingStr + "\n";
    }


    private boolean isExistTargetApiInvocation(List<String> translateCodeStrList, String targetMethodName) {
        return translateCodeStrList.stream()
                .anyMatch(translateCode -> translateCode.contains(targetMethodName));
    }
}
