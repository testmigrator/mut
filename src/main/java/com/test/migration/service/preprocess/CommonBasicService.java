package com.test.migration.service.preprocess;

import com.test.migration.entity.Constants;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
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


    public static List<String> filterTestFilepathByClassName(List<String> filepathList, String className) {
        Pattern pattern = Pattern.compile("(.*)(" + className + ")(.*)");

        return filepathList.stream().filter(sourceFilepath -> {
            String testClassFileName = CommonBasicService.getClassNameByFilepath(sourceFilepath);

            String testFileName = testClassFileName.toLowerCase();
            boolean isTestFile = testFileName.startsWith(Constants.TEST) || testFileName.endsWith(Constants.TEST)
                    || testFileName.endsWith(Constants.TESTS);
            if (!isTestFile) {
                return false;
            }

            Matcher matcher = pattern.matcher(testClassFileName);
            return matcher.find();
        }).collect(Collectors.toList());
    }

    public static List<String> filterTestFilepath(List<String> filepathList) {

        return filepathList.stream()
                .filter(filepath -> {
                    String className = CommonBasicService.getClassNameByFilepath(filepath);
                    return !className.toLowerCase().startsWith(Constants.TEST)
                            && !className.toLowerCase().endsWith(Constants.TEST);
                }).collect(Collectors.toList());
    }


    public static boolean filterNonApi(String apiName, String className) {
        String apiNameLowerCase = apiName.toLowerCase();
        boolean isConstructApi = StringUtils.equals(apiNameLowerCase, className.toLowerCase());
        if (isConstructApi) {
            return true;
        }

        boolean isNonApi = Constants.NON_API_FILTER.NON_APIS.stream()
                .anyMatch(apiNameLowerCase::startsWith);
        if (isNonApi) {
            return false;
        }

        //log
        return true;
    }

}
