package utils;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
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
