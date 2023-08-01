package com.test.migration.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum TestMethodStatus {
    DEFAULT("DEFAULT","待处理"),
    API_FUNCTION_SAME_PARAM_DIFF("API_FUNCTION_SAME_PARAM_DIFF", "api功能相同，参数不同"),
    API_CHAR_SAME_FUNCTION_DIFF("API_CHAR_SAME_FUNCTION_DIFF", "api字符相同，功能不同"),
    API_MAPPING_ERROR("API_MAPPING_ERROR", "api映射关系错误"),
    API_RELATION_WEAK("API_RELATION_WEAK", "api关联较弱"),
    TEST_COMPLEX("TEST_COMPLEX", "test过于复杂"),
    HINT_OVER_WRITE("HINT_OVER_WRITE", "hint重写"),
    HINT_FILL("HINT_FILL", "hint填充"),
    NO_MAPPING("NO_MAPPING", "无映射"),
    ;

    private final String code;
    private final String desc;

    public static String codeOf(String code){
        for(TestMethodStatus testMethodStatus : TestMethodStatus.values()){
            if(StringUtils.equals(code, testMethodStatus.getCode())){
                return testMethodStatus.getDesc();
            }
        }
        return "";
    }
}
