package com.test.migration.service;

import com.google.common.collect.Lists;
import utils.FileWriteUtil;
import utils.Log;

import java.io.File;
import java.util.List;

public class OutputReportCollector {
    static String userDir = System.getProperty("user.dir");
    static String reportOutputPath = userDir + File.separator + "output" + File.separator + "mapping" + File.separator;
    public static List<String> reports = Lists.newArrayList();

    public static void add(String line) {
        reports.add(line);
    }

    public static void clear() {
        reports = Lists.newArrayList();
    }

    public static void output(String moduleName) {
        String outputFileName = reportOutputPath + moduleName + "_report.txt";
        if (reports.isEmpty()) {
            return;
        }

        try {
            FileWriteUtil.writeDataToFile(reports, outputFileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        clear();
    }


}
