package com.test.migration.tmp;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.test.migration.entity.Constants;
import com.test.migration.entity.TaskParameter;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.preprocess.CommonBasicService;
import org.apache.commons.lang3.StringUtils;
import utils.*;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestApiMain {
    public static void main(String[] args) {
//        String sfilepath="/Users/gaoyi/IdeaProjects/staticCodeAnalysis/doc/dataset/leetcode/java/Leetcode-master/src/main/java/com/fishercoder/solutions";
//        tmpFilter(filterApiFilepathByFileType(sfilepath, Constants.FILE_TYPE.JAVA), Constants.API_BASIC_TASK.SOURCE);

        String tfilepath = "/Users/gaoyi/IdeaProjects/staticCodeAnalysis/doc/dataset/leetcode/cpp/Leetcode-masterCPPV2/src";

        tmpFilter(filterApiFilepathByFileType(tfilepath, Constants.FILE_TYPE.CPP), Constants.API_BASIC_TASK.TARGET);


    }

    public static List<String> tmpFilter(List<String> apiFilepathList, String parseType) {
        int minIndex = 1;
        int maxIndex = 100;
        List<String> tmpList = Lists.newArrayList();
        for (String filepath : apiFilepathList) {
            try {
                //        499.The-Maze-III.cpp
                if (StringUtils.equals(Constants.API_BASIC_TASK.TARGET, parseType)) {
                    String targetClassname = CommonBasicService.getClassNameByFilepath(filepath);
                    targetClassname= targetClassname.trim();
                    if(!startsWithDigit(targetClassname)){
                        continue;
                    }
                    if (targetClassname.contains("\\.")) {
                        String s = targetClassname.split("\\.")[0];
                        int i = Integer.parseInt(s.trim());
                        if (i >= minIndex && i <= maxIndex) {
                            tmpList.add(filepath);
                        }
                    } else {
                        if (targetClassname.contains(" ")) {
                            String s = targetClassname.split(" ")[0];
                            int i = Integer.parseInt(s.trim());
                            if (i >= minIndex  && i <= maxIndex) {
                                tmpList.add(filepath);
                            }
                        } else {

                            String s = targetClassname.split("-")[0];
                            int i = Integer.parseInt(s.trim());
                            if (i >= minIndex && i <= maxIndex) {
                                tmpList.add(filepath);
                            }
                        }

                    }


                }

                //        _3.java
                if (StringUtils.equals(Constants.API_BASIC_TASK.SOURCE, parseType)) {
                    String sourceClassname = CommonBasicService.getClassNameByFilepath(filepath);
                    String s = sourceClassname.split("_")[1];
                    int i = Integer.parseInt(s);
                    if (i >= minIndex && i <= maxIndex) {
                        tmpList.add(filepath);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


      return tmpList;

    }

    public static boolean containsDigit(String str) {
        return str.matches(".*\\d.*");
    }

    public static boolean startsWithDigit(String str) {
        return str.matches("\\d.*");
    }
    private static List<String> filterApiFilepathByFileType(String filePaths, String fileType) {

        List<String> filepathList = Splitter.on(Constants.SEPARATOR.COMMA)
                .splitToList(filePaths);

        // 读取指定文件类型的文件，如果指定了key，则再按key进行过滤
        return filepathList.stream()
                .flatMap(subFilepath -> {
                    List<String> files = GetFoldFileNames.readfileWithType(subFilepath, fileType);
                    return files.stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }


}
