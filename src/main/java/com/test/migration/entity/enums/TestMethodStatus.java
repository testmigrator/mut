package com.test.migration.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum TestMethodStatus {
    DEFAULT("DEFAULT","DEFAULT"),
    API_FUNCTION_SAME_PARAM_DIFF("API_FUNCTION_SAME_PARAM_DIFF", "API_FUNCTION_SAME_PARAM_DIFF"),
    API_CHAR_SAME_FUNCTION_DIFF("API_CHAR_SAME_FUNCTION_DIFF", "API_CHAR_SAME_FUNCTION_DIFF"),
    API_MAPPING_ERROR("API_MAPPING_ERROR", "API_MAPPING_ERROR"),
    API_RELATION_WEAK("API_RELATION_WEAK", "API_RELATION_WEAK"),
    TEST_COMPLEX("TEST_COMPLEX", "TEST_COMPLEX"),
    HINT_OVER_WRITE("HINT_OVER_WRITE", "HINT_OVER_WRITE"),
    HINT_FILL("HINT_FILL", "HINT_FILL"),
    NO_MAPPING("NO_MAPPING", "NO_MAPPING"),
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
