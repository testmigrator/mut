package com.test.migration.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeDiffVO {
    private String businessKey;
    private String methodName;
    private String oldCode;
    private String newCode;
    private String classMappingTips;
    private String missMappingTips;
    private String testMethodDesc;
    private String testComponentDesc;

    private String decoupleOldCode;
    private String decoupleNewCode;

    private String filepath;
    private String filename;
    private SetupTearDown setupTearDown;
    private ClassAndField classAndField;

    /**
     * success
     * fail
     * delete
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetupTearDown {
        private String oldCode;
        private String newCode;
        private String classMappingTips;
        private String missMappingTips;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassAndField {
        private String oldCode;
        private String newCode;
        private String classMappingTips;
        private String missMappingTips;
    }



}
