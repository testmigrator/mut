package com.test.migration.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonReturnType {
    private int code;

    private Object data;

    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result, 20000);
    }

    public static CommonReturnType create(Object result, int code) {
        CommonReturnType type = new CommonReturnType();
        type.setCode(code);
        type.setData(result);
        return type;
    }

}
