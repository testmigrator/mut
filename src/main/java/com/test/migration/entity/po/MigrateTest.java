package com.test.migration.entity.po;

import com.test.migration.entity.TestMethodParseBasic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrateTest {
    private Integer id;
    private Integer taskId;
    /**
     * 测试文件的路径
     */
    private String testFilepath;

    /**
     * 代码调用关系，key是具体的test方法名，value是这个test方法内部调用的存在映射关系的api的id
     *
     * json:
     * Map<String, List<Integer>>
     *  key: test_method_name
     *  value: api_ids
     */
    private String testMethodApiInvocation;

    /**
     * 记录test方法中调用的class 和 api
     *
     * json
     * Map<String, TestMethodParseBasic.ReferenceType>
     *  key: test_method_name
     *  value: @see TestMethodParseBasic ReferenceType
     *
     *  @see TestMethodParseBasic
     */
    private String referenceType;
}
