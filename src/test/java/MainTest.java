import com.google.common.base.Splitter;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainTest {
    @Test
    public void testFindPath(){
        String a = "$$$AnticipateCurve$$$$$$AnticipateCurve$$$$$$$$$$$$$$$";
        List<String> strings = Splitter.on("$$$").splitToList(a);
        strings.stream().forEach(s-> System.out.println("=== "+s+" ==="));

    }



    @Test
    public void testReadYml(){
        Yaml yaml = new Yaml();
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("test.yml");
        Map obj = yaml.load(resourceAsStream);
        Map<String,Object> param = new HashMap<>();
        //需要注意，此类加载器只能得到它的最顶层的key的值
        Map<String,Object> params= (Map) obj.get("animation");
        param.putAll(params);


        System.out.println( param.get("dataType"));
        System.out.println( param.get("testToolMethodName"));

        System.out.println("params = " + params );
    }


    class MappingRule{
        private String dataType;

    }
}
