package testCtx;

import com.google.common.collect.Lists;

import java.util.List;

public class Context {

    public static String msg;
    public static int a;
    public static List<Integer> alist = Lists.newArrayList();

    static {
        a = 0;
    }


}
