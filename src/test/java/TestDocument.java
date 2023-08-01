import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import utils.FileReadUtil;
import utils.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestDocument {

    public static void main(String[] args) throws IOException {
        String path = "/Users/gaoyi/IdeaProjects/TestMigrationV2/src/test/java/json.txt";
        int totalSize = 0;

        List<String> strings = FileReadUtil.readFile(path);
        for (String x : strings) {
            if (StringUtils.isNotBlank(x)) {
                Map<String, List<String>> map = JsonUtil.jsonToPojo(x, Map.class);
                totalSize += map.keySet().size();

            }
        }

        System.out.println(totalSize);
    }

    @Test
    public void test() throws IOException {
        String path = "/Users/gaoyi/IdeaProjects/TestMigrationV2/src/test/java/json.txt";
        String path1 = "/Users/gaoyi/IdeaProjects/TestMigrationV2/src/test/java/json1.txt";

        Set<String> set1 = Sets.newHashSet();
        Set<String> set2 = Sets.newHashSet();

        List<String> strings = FileReadUtil.readFile(path);
        List<String> strings2 = FileReadUtil.readFile(path1);
        for (String x : strings) {
            if (StringUtils.isNotBlank(x)) {
                Map<String, List<String>> map = JsonUtil.jsonToPojo(x, Map.class);
                set1.addAll(map.keySet());
            }
        }

        for (String x : strings2) {
            if (StringUtils.isNotBlank(x)) {
                Map<String, List<String>> map = JsonUtil.jsonToPojo(x, Map.class);
                set2.addAll(map.keySet());
            }
        }

        System.out.println(set1.size());
        System.out.println(set2.size());

        for (String s : set1) {
            if (!set2.contains(s)) {
                System.out.println(s);
            }
        }
        System.out.println("====");
        for (String s : set2) {
            if (!set1.contains(s)) {
                System.out.println(s);
            }
        }
    }

}
