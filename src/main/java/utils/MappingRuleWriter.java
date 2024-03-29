package utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * mappingRule
 */
public class MappingRuleWriter {


    public static void clearApiMappingProperties(){
        String filepath = ClassLoader.getSystemResource("mappingRule/apiMapping.properties").getPath();
        clear(filepath);
    }

    public static void clearClassNameMappingProperties(){
        String filepath = ClassLoader.getSystemResource("mappingRule/classNameMapping.properties").getPath();
        clear(filepath);
    }

    public static void clear(String filepath){
        try {
            FileWriter fileWriter = new FileWriter(filepath);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeApiMappingProperties(String key, String value) {
        try {
            writeProperties("mappingRule/apiMapping.properties", key, value);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeClassNameMappingProperties(String key, String value) throws IOException {
        writeProperties("mappingRule/classNameMapping.properties", key, value);
    }

//    public static void writeOtherMappingProperties(String key, String value) throws IOException {
//        writeProperties("mappingRule/otherMapping.properties", key, value);
//    }

    private static void writeProperties(String propertyName, String key, String value) throws FileNotFoundException {
        Properties properties = new Properties();

        try {
            String filepath = ClassLoader.getSystemResource(propertyName).getPath();
            Properties currentProperties = readProperties(propertyName);
            String currentValue = currentProperties.getProperty(key);
            if (StringUtils.equals(value, currentValue)) {
                return;
            }

            FileOutputStream fos = new FileOutputStream(filepath, true);
            properties.setProperty(key, value);
            properties.store(new OutputStreamWriter(fos, StandardCharsets.UTF_8), StringUtils.EMPTY);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Properties readProperties(String propertyName) {
        Properties properties = new Properties();
        InputStream in = ClassLoader.getSystemResourceAsStream(propertyName);
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return properties;
    }

}
