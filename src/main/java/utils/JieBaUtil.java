package utils;

import com.huaban.analysis.jieba.JiebaSegmenter;

public class JieBaUtil {
    public static void main(String[] args) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        System.out.println(segmenter.sentenceProcess("abc"));


    }
}
