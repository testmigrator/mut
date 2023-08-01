package utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.util.Arrays;
import java.util.List;

public class LuceneUtil {
    public static final CharArraySet ENGLISH_STOP_WORDS_SET;

    static {
        final List<String> stopWords = Arrays.asList(
                "a", "an", "are", "as", "at", "be", "but", "by",
                "for", "if", "in", "it",
                "no", "of", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with"
        );
        final CharArraySet stopSet = new CharArraySet(stopWords, false);
        ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }


    /**
     * term化
     *  删除stop word， 提取词干
     * @param text
     * @return
     * @throws Exception
     */
    public static List<String> term(List<String> text) throws Exception {
        Analyzer analyzer = new StandardAnalyzer(ENGLISH_STOP_WORDS_SET);
        TokenStream ts = analyzer.tokenStream(null, Joiner.on(" ").join(text));

        ts = new PorterStemFilter(ts);
//        OffsetAttribute offsetAttribute = ts.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
        ts.reset();//必须的
        List<String> tokens = Lists.newArrayList();
        while (ts.incrementToken()) {
//            int startOffset = offsetAttribute.startOffset();
//            int endOffset = offsetAttribute.endOffset();
            String term = charTermAttribute.toString();
            tokens.add(term);
        }
        ts.end();
        ts.close();

        return tokens;
    }
}
