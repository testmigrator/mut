package utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TokenUtil {
    private static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static void main(String[] args) {
//        camelToken("CallbackSetClipboardPixmapData");
//        camelToken("ABC_DEF_GHIi");
//        camelToken("ABC_DEF_GHI");
//        camelToken("U32_AT");
    }


    public static List<String> camelTokenize(String text) {
        if (text.contains("_")) {
            String[] splitText = text.split("_");
            return Arrays.stream(splitText)
                    .flatMap(x -> camelTokenList(x).stream())
                    .collect(Collectors.toList());
        } else {
            return camelTokenList(text);
        }
    }

    /**
     *
     * @param text
     * @return
     */
    public static List<String> camelTokenList(String text) {
        boolean existUpperChar = existUpperChar(text);
        if (!existUpperChar) {
            return Lists.newArrayList(text);
        }

        boolean isFirstCharEng = isFirstCharEng(text);
        if(!isFirstCharEng){
            return Lists.newArrayList(text);
        }

        String first = String.valueOf(text.charAt(0)).toUpperCase();
        text = first + text.substring(1);
        List<String> tokens = Lists.newArrayList();
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            boolean isUpperCase = Character.isUpperCase(text.charAt(i));
            if (isUpperCase) {
                tmp.append(text.charAt(i));
                while (i < text.length() - 1 && !Character.isUpperCase(text.charAt(++i))) {
                    tmp.append(text.charAt(i));
                }
            }
            if (i == text.length() - 1) {
                tokens.add(tmp.toString());
                break;
            }
            i--;
            tokens.add(tmp.toString());
            tmp = new StringBuilder();
        }

        return tokens.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private static boolean isFirstCharEng(String text) {
        return letters.contains(String.valueOf(text.charAt(0)));
    }

    private static boolean existUpperChar(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static double tokenSeqSimilarity(List<String> sources, List<String> targets) {
        int similarTokenNum = 0;
        for (String source : sources) {
            if (targets.contains(source)) {
                similarTokenNum = similarTokenNum + 2;
            }
        }
        int total = sources.size() + targets.size();
        return (double) similarTokenNum / total;
    }


    /**
     */
    public static String camelize(String tabAttr) {
        if (StringUtils.isBlank(tabAttr))
            return tabAttr;
        Pattern pattern = Pattern.compile("(.*)_(\\w)(.*)");
        Matcher matcher = pattern.matcher(tabAttr);
        if (matcher.find()) {
            return camelize(matcher.group(1) + matcher.group(2).toUpperCase() + matcher.group(3));
        } else {
            return tabAttr;
        }
    }

    /**
     */
    public static String decamelize(String camelCaseStr){
        return StringUtils.isBlank(camelCaseStr) ? camelCaseStr : camelCaseStr.replaceAll("[A-Z]", "_$0").toLowerCase();
    }

    /**
     */
    public static List<String> decamelizeToList(String camelCaseStr){
        String decamelizeStr = decamelize(camelCaseStr);
        return Arrays.stream(decamelizeStr.split("_"))
                .collect(Collectors.toList());
    }

}
