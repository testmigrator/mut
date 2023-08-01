package utils;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GetFoldFileNames {
    /**
     * 读取某个文件夹下的所有文件
     */
    public static List<String> readfile(String filepath) {
        return readfileWithType(filepath, "java");
    }

    public static List<String> readfileWithType(String filepath, String type) {
        List<String> fileNames = Lists.newArrayList();
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                System.out.println(filepath + " no files");
                return Lists.newArrayList();
            }

            if (file.isFile() && filepath.endsWith(type)) {
                fileNames.add(filepath);
            }

            if (file.isDirectory()) {
                String[] filelist = file.list();
                for (int i = 0; i < Objects.requireNonNull(filelist).length; i++) {
                    File readfile = new File(filepath + "/" + filelist[i]);
                    if (!readfile.isDirectory()) {
                        String fileName = readfile.getName();
                        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                        if (suffix.equals(type)) {
                            fileNames.add(filepath + "/" + filelist[i]);
                        }
                    } else if (readfile.isDirectory()) {
                        fileNames.addAll(readfileWithType(filepath + "/" + filelist[i], type));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("readfile()  Exception:" + e.getMessage());
        }
        return fileNames;
    }
}