package utils;

import com.google.common.collect.Lists;
import com.test.migration.service.translate.MappingRuleLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

public class MappingRuleReader {
    public static void main(String[] args) {
        List<String> apiMappingStr = MappingRuleReader.readLinesFromProperties("mappingRule/apiMapping.properties");
        apiMappingStr.stream()
                .map(x->x.replace("\\:",":"))
                .forEach(x-> System.out.println(x));
    }


    public static List<String> readLinesFromProperties(String propertyName) {
        List<String> lines = Lists.newArrayList();
        String filepath = ClassLoader.getSystemResource(propertyName).getPath();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(filepath));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }
                lines.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return lines.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }
}
