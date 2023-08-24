package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    public static void info(String msg) {
        getLogger().info(msg);
    }

    public static void warn(String msg) {
        getLogger().warn(msg);
    }

    public static void error(String msg) {
        getLogger().error(msg);
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(findCaller().getClassName());
    }

    public static String getClassName() {
        return new SecurityManager() {
            public String getClassName() {
                return getClassContext()[1].getName();
            }
        }.getClassName();
    }

    private static StackTraceElement findCaller(){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String logClassName = Log.class.getName();


        boolean flag = true;
        int i = 0;
        //以LogUtil本身为基准
        //查找到最后一个LogUtil的堆栈信息的后一个退出
        while(flag || logClassName.equals((stackTraceElements[i].getClassName()))){
            if(logClassName.equals((stackTraceElements[i].getClassName()))){
                //相等时变为false
                flag = false;
            }
            i++;
        }
        return stackTraceElements[i];
    }


}
