package com.test.migration.entity.po;

import com.test.migration.entity.enums.TestMethodStatus;
import lombok.*;

/**
 * 页面展示代码迁移前后的结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TestMethodDiff {
    private Integer id;
    /**
     * filepath+$+startLine
     * 用于标识TestMethodDiff，方便后续操作（更新字段等）
     */
    private String businessKey;
    private String filepath;
    private String methodName;
    /**
     * 迁移前代码
     */
    private String oldCode;
    /**
     * 迁移后代码
     */
    private String newCode;
    /**
     * chatgpt生成的test方法功能描述
     */
    private String testMethodDesc;
    /**
     * chatgpt生成的test方法内部组件（class，method）解释
     */
    private String testComponentDesc;
    /**
     * class 可能出现多mapping，这种情况下会形成tip在页面展示
     */
    private String classMappingTips;
    /**
     * 提示哪些组件没有存在代码匹配关系
     */
    private String missMappingTips;
    /**
     * @see TestMethodStatus
     */
    private String status;
    /**
     * 0:test method
     * 1:other method
     * 2:class
     * 3:field
     * 4:setUp/tearDown
     */
    private String type;
    /**
     * 测试解耦：chatgpt对一个test拆分后的多个test
     */
    private String decoupleOldCode;
    /**
     * 对测试解耦后的test进行迁移
     */
    private String decoupleNewCode;
}
