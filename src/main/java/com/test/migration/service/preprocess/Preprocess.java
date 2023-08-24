package com.test.migration.service.preprocess;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import utils.LuceneUtil;
import utils.TokenUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class Preprocess {


    public static List<String> preprocess(String name) {
        List<String> terms = Lists.newArrayList();
        try {
            List<String> tokens = generateWordSequence(name);

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
            terms = LuceneUtil.term(tokens);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return terms;
    }

    /**
     *
     * @param name
     * @return
     */
    public static List<String> generateWordSequence(String name) {
        String camelize = TokenUtil.camelize(name);
        return TokenUtil.decamelizeToList(camelize).stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }


}
