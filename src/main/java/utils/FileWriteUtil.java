package utils;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

public class FileWriteUtil {

    public static void writeDataToFile(List<String> dataLines, String filePath) throws Exception {
        BufferedWriter bufferedWriter = null;
        bufferedWriter = new BufferedWriter(new FileWriter(filePath, true));
        for (String dataLine : dataLines) {
            bufferedWriter.write(dataLine + "\n");
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    /**
     * tmp
     * @param dataLines
     * @param filePath
     * @throws Exception
     */
    public static void writeDataToFileWithoutAppend(List<String> dataLines, String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            boolean newFile = file.createNewFile();
        }
        BufferedWriter bufferedWriter = null;
        bufferedWriter = new BufferedWriter(new FileWriter(filePath, false));
        for (String dataLine : dataLines) {
            bufferedWriter.write(dataLine + "\n");
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }


}
