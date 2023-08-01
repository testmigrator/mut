package com.test.migration.controller;

import com.google.common.collect.Lists;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import com.test.migration.entity.response.CommonReturnType;
import com.test.migration.entity.vo.ApiMappingVO;
import com.test.migration.service.ApiBasicService;
import com.test.migration.service.ApiMappingService;
import com.test.migration.service.translate.MappingRuleLoader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/apiMapping")
public class ApiMappingController {
    ApiBasicService apiBasicService = new ApiBasicService();
    ApiMappingService apiMappingService = new ApiMappingService();

    @GetMapping(value = "/list")
    public CommonReturnType listAllMappings() {
        List<ApiBasic> apiBasics = apiBasicService.selectByTaskId(1);
        Map<Integer, ApiBasic> apiBasicMap = apiBasics.stream().collect(Collectors.toMap(ApiBasic::getId, Function.identity()));
        List<ApiMapping> apiMappings = apiMappingService.selectByTaskId(1);

        List<ApiMappingVO> apiMappingVOS =  testList.stream()
                .map(xx->{
                    String[] split = xx.split("," );
                    return ApiMappingVO.builder()
                            .sourceClassName(split[0])
                            .sourceMethodName(split[1])
                            .targetClassName(split[2])
                            .targetMethodName(split[3])
                            .status(split[4])
                            .build();
                })
                .collect(Collectors.toList());

       return CommonReturnType.create(apiMappingVOS);
//
//        List<ApiMappingVO> apiMappingVOS = apiMappings.stream().map(x -> {
//                    ApiBasic sourceAPI = apiBasicMap.get(x.getSourceApiId());
//                    ApiBasic targetAPI = apiBasicMap.get(x.getTargetApiId());
//                    if (sourceAPI == null || targetAPI == null) {
//                        return null;
//                    }

//                    return ApiMappingVO.builder()
//                            .sourceClassName(sourceAPI.getClassName())
//                            .sourceMethodName(sourceAPI.getApiName())
//                            .targetClassName(targetAPI.getClassName())
//                            .targetMethodName(targetAPI.getApiName())
//                            .build();
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        return CommonReturnType.create(apiMappingVOS);
    }


    static List<String> testList = Lists.newArrayList(
            "Element,select,node,select,Enable",
            "Element,append,node,append,Enable",
//            "Element,empty,node,empty,Enable",
            "Element,text,node,to_text,Enable",
            "Element,html,node,to_html,Enable",
            "Jsoup,parse,parser,parse,Enable",
            "Attributes,add,node,append,Disable"
    );




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiMappingVO {
        private String sourceClassName;
        private String sourceMethodName;
        private String targetClassName;
        private String targetMethodName;
        private String status;
    }


}
