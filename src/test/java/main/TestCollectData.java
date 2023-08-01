package main;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import com.test.migration.entity.po.MigrateTest;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.TranslateTestService;
import org.testng.annotations.Test;
import utils.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestCollectData {
    ApiMappingService apiMappingService = new ApiMappingService();
    ApiBasicService apiBasicService = new ApiBasicService();
    TranslateTestService translateTestService = new TranslateTestService();


    @Test
    public void calcTestNum(){


    }

    @Test
    public void testShowMapping() {
        int taskId = 1;
        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(taskId);
        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(taskId);

        Map<Integer, ApiBasic> apiBasicMap =
                apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));
        for (ApiMapping apiMapping : apiMappings) {
            ApiBasic sourceApi = apiBasicMap.get(apiMapping.getSourceApiId());
            ApiBasic targetApi = apiBasicMap.get(apiMapping.getTargetApiId());
            System.out.println(sourceApi.getClassName() + " - " + sourceApi.getApiName() + " : " + targetApi.getClassName() + " - " + targetApi.getApiName());
        }
    }


}
