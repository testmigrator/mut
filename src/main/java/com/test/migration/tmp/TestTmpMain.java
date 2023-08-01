package com.test.migration.tmp;

import com.test.migration.entity.po.ApiBasic;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.TranslateTestService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import utils.MappingRuleWriter;

import java.util.List;
import java.util.stream.Collectors;

public class TestTmpMain {
    ApiBasicService apiBasicService = new ApiBasicService();
    static TranslateTestService translateTestService = new TranslateTestService();

    public static void main(String[] args) {
        // 生成api关联test信息
//        translateTestService.generateMigrateTest();
    }


    @Test
    public void extractSourceApiBasic() {
        MappingRuleWriter.clearApiMappingProperties();
        MappingRuleWriter.clearClassNameMappingProperties();

        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
        List<Integer> deleteIds = apiBasics.stream()
                .filter(x -> x.getType() == 0 || x.getType() == 3)
                .map(ApiBasic::getId)
                .collect(Collectors.toList());
        deleteIds.forEach(id -> apiBasicService.deleteById(id));

//        apiBasicService.extractSourceApiBasic();

        calculateApiMappings();
    }

    @Test
    public void extractTargetApiBasic() {
//        MappingRuleWriter.clearApiMappingProperties();
//        MappingRuleWriter.clearClassNameMappingProperties();
//
//        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
//        List<Integer> deleteIds = apiBasics.stream()
//                .filter(x -> x.getType() == 1 || x.getType() == 4)
//                .map(ApiBasic::getId)
//                .collect(Collectors.toList());
//
//        deleteIds.forEach(id -> apiBasicService.deleteById(id));

//        apiBasicService.extractTargetApiBasic();

        calculateApiMappings();
    }

    public void calculateApiMappings(){
        ApiMappingService apiMappingService = new ApiMappingService();
        apiMappingService.delete();
        // 生成api的vector并保存
        apiMappingService.calculateCodeMappings();
    }

    @Test
    public void showSourceApi() {

        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1).stream()
                .filter(x-> StringUtils.equals("Parser",x.getClassName()))
                .filter(x -> x.getType() == 0 || x.getType() == 2)
                .collect(Collectors.toList());

        apiBasics.forEach(x-> {
            System.out.println(x.getApiName());

        });

    }
}
