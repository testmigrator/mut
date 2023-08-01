package com.test.migration.entity;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskParameter {

    private Integer taskId;
    /**
     * 用于mapping的source,支持多个目录，用逗号分割
     */
    private String sourceFilepath;
    /**
     * 用于mapping的target, 支持多个目录，用逗号分割
     */
    private String targetFilepath;
    /**
     * 用于解析test文件，所有sourceCode,支持多个目录，用逗号分割
     */
    private String targetSourceCodeFilepath;

    /**
     * TODO
     * filepath key
     */
    private String filepathKey;

    /**
     * 迁移结果输出文件路径
     */
    private String outputFilepath;

    /**
     * python 命令路径配置
     */
    private String pythonBinPath;

    /**
     * token序列语料库文件路径
     */
    private String corpusFilepath;
    /**
     * token向量字典文件路径
     */
    private String wordVecModelFilepath;

    /**
     * api name和对应生成的向量字典文件路径
     */
    private String apiVectorDictFilepath;
    /**
     * class name和对应生成的向量字典文件路径
     */
    private String classVectorDictFilepath;

    /**
     * class 文档生成的向量字典文件路径
     */
    private String classCommentVectorDictFilepath;

    /**
     * method 文档生成的向量字典文件路径
     */
    private String methodCommentVectorDictFilepath;

    /**
     * method 输入和输出 生成的向量字典文件路径
     */
    private String methodParamReturnVectorDictFilepath;

    /**
     * word list用于近义词+KM算法找api mapping
     */
    private String apiWordListFilepath;

//    private Double similarityThreshold;

    /**
     * sqllite db文件的路径地址
     */
    private String dbFilepath;


    /**
     * python
     */
    private String pythonCppExtractor;
    private String pythonWordVec;
    private String pythonCalcTokenVec;
    private String pythonCalcSimilarity;
}
