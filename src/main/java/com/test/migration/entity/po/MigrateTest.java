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
    private String testFilepath;

    /**
     *
     * json:
     * Map<String, List<Integer>>
     *  key: test_method_name
     *  value: api_ids
     */
    private String testMethodApiInvocation;

    /**
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
