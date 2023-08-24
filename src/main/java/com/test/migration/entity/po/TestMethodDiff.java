package com.test.migration.entity.po;

import com.test.migration.entity.enums.TestMethodStatus;
import lombok.*;

/**
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
     */
    private String businessKey;
    private String filepath;
    private String methodName;
    private String oldCode;
    private String newCode;
    private String testMethodDesc;
    private String testComponentDesc;

    private String classMappingTips;
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
    private String decoupleOldCode;
    private String decoupleNewCode;
}
