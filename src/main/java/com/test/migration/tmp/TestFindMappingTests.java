package com.test.migration.tmp;

import com.google.common.collect.Lists;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import com.test.migration.entity.po.MigrateTest;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.MigrateTestService;
import com.test.migration.service.TranslateTestService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import utils.JsonUtil;
import utils.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestFindMappingTests {
    @Test
    public void testShowMappings() {
        ApiBasicService apiBasicService = new ApiBasicService();
        ApiMappingService apiMappingService = new ApiMappingService();
        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
        Map<Integer, ApiBasic> apiBasicMap = apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));

        apiMappingService.selectByTaskId(1).stream()
                .map(x -> {
                    ApiBasic sourceApi = apiBasicMap.get(x.getSourceApiId());
                    ApiBasic targetApi = apiBasicMap.get(x.getTargetApiId());
                    return sourceApi.getApiName() + "   " + sourceApi.getClassName() + "    |    " + targetApi.getApiName() + "   " + targetApi.getClassName();
                }).distinct().forEach(System.out::println);
    }


    public static void main(String[] args) {
        String apiMappingStr = "isEqual   ReadableInstant    |    is_equal   counted_time_system";
        String[] mappingSplit = apiMappingStr.split("\\|");
        List<String> source = Arrays.stream(mappingSplit[0].split(" ")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> target = Arrays.stream(mappingSplit[1].split(" ")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        String sourceClassName = source.get(1).trim();
        String sourceApiName = source.get(0).trim();
        String targetClassName = target.get(1).trim();
        String targetApiName = target.get(0).trim();

        ApiBasicService apiBasicService = new ApiBasicService();
        ApiMappingService apiMappingService = new ApiMappingService();
        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
        Map<Integer, ApiBasic> apiBasicMap = apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));

        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(1).stream()
                .filter(x -> {
                    ApiBasic sourceApi = apiBasicMap.get(x.getSourceApiId());
                    ApiBasic targetApi = apiBasicMap.get(x.getTargetApiId());
                    return StringUtils.equals(sourceClassName, sourceApi.getClassName())
                            && StringUtils.equals(sourceApiName, sourceApi.getApiName())
                            && StringUtils.equals(targetApiName, targetApi.getApiName())
                            && StringUtils.equals(targetClassName, targetApi.getClassName());
                })
                .collect(Collectors.toList());
        if (apiMappings.size() != 1) {
            Log.error("mapping 出错！");
        }

        ApiMapping apiMapping = apiMappings.get(0);

        MigrateTestService translateTestService = new MigrateTestService();
        List<MigrateTest> migrateTests = translateTestService.selectByTaskId(1);

        // 收集该api映射关联的test
        List<String> testInfos = Lists.newArrayList();
        for (MigrateTest x : migrateTests) {
            Map<String, List<Integer>> map = JsonUtil.jsonToPojo(x.getTestMethodApiInvocation(), Map.class);
            if (map == null) {
                continue;
            }
            for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
                String k = entry.getKey();
                List<Integer> v = entry.getValue();
                if (v.contains(apiMapping.getSourceApiId())) {
                    testInfos.add(x.getTestFilepath() + "\n");
                    testInfos.add(k + "\n");
                }
            }
        }
        testInfos = testInfos.stream().distinct().collect(Collectors.toList());

        if (testInfos.size() <= 1) {
            Log.error("该映射api没有test");
        } else {
            Log.warn("该映射api存在以下几个test");
            testInfos.forEach(System.out::println);
        }
    }
}
