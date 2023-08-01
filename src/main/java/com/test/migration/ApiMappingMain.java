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
    // 获取api基础信息
    ApiBasicService apiBasicService = new ApiBasicService();
    // 生成mapping关系
    ApiMappingService apiMappingService = new ApiMappingService();


    public static void main(String[] args) {
        ApiMappingMain main = new ApiMappingMain();
        main.process();
    }

    public void process(){
        // 清空历史code mapping
        MappingRuleWriter.clearApiMappingProperties();
        MappingRuleWriter.clearClassNameMappingProperties();

        // 初始化（删除，新建）api，mapping，translate表
        MyBatisUtil.initTable();

        // 从源码文件提取api信息
        List<String> sourceFileTypes = Lists.newArrayList(Constants.FILE_TYPE.JAVA);
        List<String> targetFileTypes = Lists.newArrayList(Constants.FILE_TYPE.CPP_HEAD_H, Constants.FILE_TYPE.CPP_HEAD_HPP);
//        List<String> targetFileTypes = Lists.newArrayList(Constants.FILE_TYPE.CPP);
        apiBasicService.extractApiBasic(sourceFileTypes, targetFileTypes);

        // 计算api mapping
        apiMappingService.calculateCodeMappings();
    }

}
