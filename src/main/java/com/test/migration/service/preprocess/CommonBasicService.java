package com.test.migration.service.preprocess;

import com.test.migration.entity.Constants;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 通用处理，比如：
 * 根据文件路径获取文件名
 */
public class CommonBasicService {

    public static String getClassNameByFilepath(String filepath) {
        if (StringUtils.isBlank(filepath)) {
            return StringUtils.EMPTY;
        }
        String[] split = filepath.split(File.separator);
        String classFileName = split[split.length - 1];
        return classFileName.split("\\.")[0];
    }

    /**
     * 通过常规Test命名规则来获取test类：
     * AClass对应的测试类为：TestAClass 或者 AClassTest
     * <p>
     * 但是这个规则不一定找出所有的test，测试文件名不一定按照规范来设定，比如
     * <p>
     * AnimatorSet对应的测试类AnimatorSetActivityTest目前是匹配不出来的
     * 补充规则：为了匹配到这种命名，使用  ：
     * *（通配符）+类名+*（通配符）+Test（前后缀）的方式来匹配测试类文件
     *
     * @return 对于同一个class的test文件可能不止一个，因此返回List
     */
    public static List<String> filterTestFilepathByClassName(List<String> filepathList, String className) {
        Pattern pattern = Pattern.compile("(.*)(" + className + ")(.*)");

        return filepathList.stream().filter(sourceFilepath -> {
            String testClassFileName = CommonBasicService.getClassNameByFilepath(sourceFilepath);

            // 必须带有test前后缀，否则视为非测试文件
            String testFileName = testClassFileName.toLowerCase();
            boolean isTestFile = testFileName.startsWith(Constants.TEST) || testFileName.endsWith(Constants.TEST)
                    || testFileName.endsWith(Constants.TESTS);
            if (!isTestFile) {
                return false;
            }

            // 正则匹配
            Matcher matcher = pattern.matcher(testClassFileName);
            return matcher.find();
        }).collect(Collectors.toList());
    }

    /**
     * 过滤掉test文件
     * @param filepathList
     * @return
     */
    public static List<String> filterTestFilepath(List<String> filepathList) {

        return filepathList.stream()
                .filter(filepath -> {
                    String className = CommonBasicService.getClassNameByFilepath(filepath);
                    return !className.toLowerCase().startsWith(Constants.TEST)
                            && !className.toLowerCase().endsWith(Constants.TEST);
                }).collect(Collectors.toList());
    }

    /**
     * 构造函数也应被视为API
     * 过滤掉get/set/toString  (会存在一点误伤，比如一些功能性api命名就是setxxx。更合理的方式是考虑代码实现情况)
     *
     * @return false表示要过滤掉
     */
    public static boolean filterNonApi(String apiName, String className) {
        // 过滤掉构造函数
        String apiNameLowerCase = apiName.toLowerCase();
        boolean isConstructApi = StringUtils.equals(apiNameLowerCase, className.toLowerCase());
        if (isConstructApi) {
            return true;
        }

        // 过滤掉get/set/toString
        boolean isNonApi = Constants.NON_API_FILTER.NON_APIS.stream()
                .anyMatch(apiNameLowerCase::startsWith);
        if (isNonApi) {
            return false;
        }

        //log
        return true;
    }

}
