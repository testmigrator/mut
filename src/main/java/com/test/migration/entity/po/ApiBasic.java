package com.test.migration.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiBasic {

    private Integer id;

    private Integer taskId;

    private String filepath;

    private String className;

    private String apiName;

    /**
     * 0：source api
     * 1：target api
     * 3：source get/set/tostring
     * 4：target get/set/tostring
     */
    private Integer type;

    /**
     */
    private String methodWordSequence;

    private String tokenSequence;

    @Deprecated
    private String tokenVector;

    private String classNameTokenSequence;

    private String classCommentTokenSequence;

    private String methodCommentTokenSequence;

    private String methodParamReturnTokenSequence;


}
