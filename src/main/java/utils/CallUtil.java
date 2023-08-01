package utils;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * java调用shell，python等
 */
public class CallUtil {

    public static List<String> callCMD(String cmd){
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        String prefix1 = isWindows ? "cmd" : "/bin/sh";
        String prefix2 = isWindows ? "/c" : "-c";
        return call(new String[]{prefix1, prefix2, cmd});
    }


    /**
     * String[] args1=new String[]{"/home/huan/anaconda2/bin/python","/home/huan/myfile/pythonfile/helloword.py"};
     * Process pr=Runtime.getRuntime().exec(args1);
     */
    public static List<String> call(String[] args) {
        List<String> resultLines = Lists.newArrayList();

        try {
        	/*
			附加：
			String数组里的那一行很重要
			首先一定要设置好你所使用的python的位置，切记不要直接使用python，因为系统会默认使用自带的python，所以一定要设置好你所使用的
			python的位置，否则可能会出现意想不到的问题（比如说我使用的是anaconda中的python，而ubuntu系统会默认调用自带的python，
			而我自带的python中并没有numpy库，所以会造成相应的代码不会执行的问题，所以设置好python的位置是很重要的）。
			还有就是要设置好py文件的位置，使用绝对路径。在这里插入代码片
            还有就是可以看出，此方法可以满足我们python代码中调用第三方库的情况，简单实用。
			*/
//            proc = Runtime.getRuntime().exec("python ./plus.py");
            Process pr = Runtime.getRuntime().exec(args);
//            Arrays.stream(args).forEach(arg->System.out.print(arg+" "));
            BufferedReader errorIn = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String errorLine;
            while ((errorLine = errorIn.readLine()) != null) {
                System.out.println(errorLine);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                resultLines.add(line);
            }
            in.close();
            pr.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return resultLines;
    }
}
