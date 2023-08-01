import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonTest {

    @Test
    public void testFindTestFile(){
        String className1 = "AnimatorSet";
        String className2 = "Animator";
        String testFileName = "AnimatorSetActivityTest";

//        Pattern pattern = Pattern.compile("(.*)("+className1+")(.*)");
        Pattern pattern = Pattern.compile("(.*)("+className2+")(.*)");
        Matcher matcher = pattern.matcher(testFileName);
        System.out.println(matcher.find());
//        if (matcher.find()) {
//            return camelize(matcher.group(1) + matcher.group(2).toUpperCase() + matcher.group(3));

    }
}
