package com.test.migration.service.preprocess;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import utils.LuceneUtil;
import utils.TokenUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * todo stopwords的设计
 */
public class Preprocess {

    /**
     * 预处理过程
     * -切分为词序列 eg：equalsIgnoreCase => equals ignore case
     * -删除stop words eg:删除数字
     * -替换为词干（Lancaster） eg: representing => repres
     *
     * @param name 类名，方法名，变量名等
     */
    public static List<String> preprocess(String name) {
        List<String> terms = Lists.newArrayList();
        try {
            // 切分为词序列
            List<String> tokens = generateWordSequence(name);

            // 删除stop words, 提取词干并做替换
            terms = LuceneUtil.term(tokens);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // terms
        return terms;
    }


    public static List<String> preprocessPhrase(String phrase) {
        List<String> terms = Lists.newArrayList();
        String formatPhrase = phrase.toLowerCase()
                .replace(",", " ")
                .replace(".", " ")
                .replace(":", " ")
                .replace("!", " ")
                .replace("?", " ")
                .replace("'", " ")
                .replace("@", " ");
        List<String> tokens = Splitter.on(" ").splitToList(formatPhrase).stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        try {
            // 删除stop words, 提取词干并做替换
            terms = LuceneUtil.term(tokens);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return terms;
    }

    /**
     * 原单词，没有替换为词干
     *
     * @param name
     * @return
     */
    public static List<String> generateWordSequence(String name) {
        String camelize = TokenUtil.camelize(name);
        // 切分为词序列
        return TokenUtil.decamelizeToList(camelize).stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }


}
