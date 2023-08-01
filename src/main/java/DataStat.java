import com.google.common.collect.Lists;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.MigrateTestService;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataStat {

    static ApiBasicService apiBasicService = new ApiBasicService();
    static ApiMappingService apiMappingService = new ApiMappingService();
    static MigrateTestService migrateTestService = new MigrateTestService();



    @Test
    public void totalAPIMapping(){
        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(1);
        System.out.println("total api mapping :"+apiMappings.size());
    }
    static List<String> testList = Lists.newArrayList(
            "Element,select,node,select",
            "Element,append,node,append",
            "Element,empty,node,empty",
            "Element,text,node,to_text",
            "Element,html,node,to_text",
            "Element,select,node,to_html",
            "Jsoup,parse,parser,parse",
            "Attributes,add,node,append"
            );

    @Test
    public void totalTests(){
        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(1);

        Map<Integer, ApiMapping> apiMappingsMap =
                apiMappings.stream().collect(Collectors.toMap(ApiMapping::getId, Function.identity()));


        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
        Map<Integer, ApiBasic> apiBasicMap =
                apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));
        List<Integer> ids = Lists.newArrayList(1,4,6,7,8,21);
        ids.forEach(x-> {
            ApiMapping apiMapping = apiMappingsMap.get(x);
            System.out.println(apiBasicMap.get(apiMapping.getSourceApiId()).getClassName()
            +"$"+apiBasicMap.get(apiMapping.getSourceApiId()).getApiName()
            +"===="+apiBasicMap.get(apiMapping.getTargetApiId()).getClassName()
            +"$"+apiBasicMap.get(apiMapping.getTargetApiId()).getApiName()
            );


        });

        for (ApiMapping apiMapping :apiMappings
        ) {
            ApiBasic source = apiBasicMap.get(apiMapping.getSourceApiId());
            ApiBasic target = apiBasicMap.get(apiMapping.getTargetApiId());
            for (int i = 0; i <testList.size(); i++) {
                String line = testList.get(i);
                String[] split = line.split("," );

                if(source.getClassName().equals(split[0])
                && source.getApiName().equals(split[1])
                && target.getClassName().equals(split[2])
                && target.getApiName().equals(split[3])

                ){
                    System.out.print(apiMapping.getId()+",");
                }

            }

        }



    }

    @Test
    public void migrateTotalTests(){

    }







}
