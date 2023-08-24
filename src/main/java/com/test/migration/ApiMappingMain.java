package com.test.migration;

import com.google.common.collect.Lists;
import com.test.migration.entity.Constants;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.MigrateTestService;
import com.test.migration.service.TranslateTestService;
import utils.MappingRuleWriter;
import utils.MyBatisUtil;

import java.util.List;

public class ApiMappingMain {
    ApiBasicService apiBasicService = new ApiBasicService();
    ApiMappingService apiMappingService = new ApiMappingService();


    public static void main(String[] args) {
        ApiMappingMain main = new ApiMappingMain();
        main.process();
    }

    public void process(){
        MappingRuleWriter.clearApiMappingProperties();
        MappingRuleWriter.clearClassNameMappingProperties();

        MyBatisUtil.initTable();

        List<String> sourceFileTypes = Lists.newArrayList(Constants.FILE_TYPE.JAVA);
        List<String> targetFileTypes = Lists.newArrayList(Constants.FILE_TYPE.CPP_HEAD_H, Constants.FILE_TYPE.CPP_HEAD_HPP);
        apiBasicService.extractApiBasic(sourceFileTypes, targetFileTypes);

        apiMappingService.calculateCodeMappings();
    }

}
