package utils;

import com.huaban.analysis.jieba.JiebaSegmenter;

public class JieBaUtil {
    public static void main(String[] args) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        /*单词*/
        System.out.println(segmenter.sentenceProcess("小明硕士毕业于中国科学院计算所，后在日本京都大学深造"));
        System.out.println(segmenter.sentenceProcess("这是一个伸手不见五指的黑夜"));
        /*多词*/
        /*String[] sentences =
                new String[] {"这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", "我不喜欢日本和服。", "雷猴回归人间。",
                        "工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作", "结果婚的和尚未结过婚的"};
        for (String sentence : sentences) {
            System.out.println(segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX).toString());
        }*/

    }
}
