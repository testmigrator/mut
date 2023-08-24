package mapping.caseres;

import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestCase {

    public static void main(String[] args) {
        showcase();

    }

    private static void showcase() {
        ApiBasicService apiBasicService = new ApiBasicService();
        ApiMappingService apiMappingService = new ApiMappingService();

        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(1);

        Map<Integer, ApiBasic> queryApiMap = apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId,Function.identity()));
        for (ApiMapping apiMapping: apiMappings) {
            ApiBasic source = queryApiMap.get(apiMapping.getSourceApiId());
            ApiBasic target = queryApiMap.get(apiMapping.getTargetApiId());

            System.out.println(apiMapping.getId()+" "+source.getClassName()+" $ "+ source.getApiName() +" : "+target.getClassName()+" $ "+target.getApiName());
        }
    }
}
