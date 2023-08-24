package com.test.migration.entity;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskParameter {

    private Integer taskId;
    private String sourceFilepath;
    private String targetFilepath;
    private String targetSourceCodeFilepath;

    /**
     * TODO
     * filepath key
     */
    private String filepathKey;

    private String outputFilepath;
    private String pythonBinPath;
    private String corpusFilepath;
    private String wordVecModelFilepath;
    private String apiVectorDictFilepath;
    private String classVectorDictFilepath;
    private String classCommentVectorDictFilepath;
    private String methodCommentVectorDictFilepath;
    private String methodParamReturnVectorDictFilepath;
    private String apiWordListFilepath;

//    private Double similarityThreshold;
    private String dbFilepath;


    /**
     * python
     */
    private String pythonCppExtractor;
    private String pythonWordVec;
    private String pythonCalcTokenVec;
    private String pythonCalcSimilarity;
}
