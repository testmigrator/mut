package com.test.migration.service.translate;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslateHint {
    public static final String BS_HINT_TAG = "-BSHINT-";

    public static final String MIS_MATCH_TIPS = "迁移失效：以下安卓组件代码在鸿蒙中无法找到相匹配的映射";
    public static List<String> misMatchCodes;
    public static int startLine = 0;
    public static int endLine = 0;

    /**
     * 在translate之前，先调用init
     */
    public static void init() {
        misMatchCodes = Lists.newArrayList();
        startLine = 0;
        endLine = 0;
    }

    public static String misMatchCodesToString() {
        if (CollectionUtils.isEmpty(misMatchCodes)) {
            return "";
        }
        return Joiner.on(",").join(misMatchCodes);
    }

    public static List<String> formatMisMatchCodes(List<String> misMatchCodes){
        return misMatchCodes.stream()
                .filter(x-> !StringUtils.equals(x, BS_HINT_TAG))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }
}



