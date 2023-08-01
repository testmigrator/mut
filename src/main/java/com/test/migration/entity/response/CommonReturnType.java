package com.test.migration.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO completion javadoc.
 *
 * @author gaoyi
 * @since 19 April 2020
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonReturnType {
    // 表明对应请求的返回处理结果success和fail
    private int code;

    // 若status = success，则data内返回前端需要的json数据
    // 若status = fail, 则data内使用通用的错误格式
    private Object data;

    //定义一个通用的创建方法
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
