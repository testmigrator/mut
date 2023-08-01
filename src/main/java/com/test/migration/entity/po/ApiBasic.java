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
     * 方法单词序列，用逗号分割
     */
    private String methodWordSequence;

    /**
     * 词干序列，用逗号分割
     * ！！方法名！！
     */
    private String tokenSequence;

    /**
     * 没必要存数据库，用不着而且会降低运行效率
     */
    @Deprecated
    private String tokenVector;

    /**
     * 用于二级筛选mapping，比如：
     * harmony api：Animator#start
     * android api：AnimatorSet#start，Animator#start
     * 预期结果：Animator#start 和 Animator#start
     */
    private String classNameTokenSequence;

    private String classCommentTokenSequence;

    private String methodCommentTokenSequence;

    private String methodParamReturnTokenSequence;


}
