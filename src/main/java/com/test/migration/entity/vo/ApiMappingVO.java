package com.test.migration.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMappingVO {
    private String sourceClassName;
    private String sourceMethodName;
    private String targetClassName;
    private String targetMethodName;
}
