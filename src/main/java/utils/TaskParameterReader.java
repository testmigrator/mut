package utils;

import com.google.common.base.Splitter;
import com.test.migration.entity.TaskParameter;
import com.test.migration.service.OutputReportCollector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class TaskParameterReader {
    public static TaskParameter taskParameter;

    public static TaskParameter getTaskParameter() {
        if (taskParameter != null) {
            return taskParameter;
        }
        taskParameter = buildTaskParameter();
        return taskParameter;
    }

    public static void addParamToReport(TaskParameter taskParameter) {
        if(taskParameter == null){
            return;
        }
        List<String> sourceFiles = Splitter.on(",").splitToList(taskParameter.getSourceFilepath());
        List<String> testFiles = Splitter.on(",").splitToList(taskParameter.getTargetSourceCodeFilepath());
        sourceFiles.forEach(OutputReportCollector::add);

        OutputReportCollector.add(taskParameter.getTargetFilepath());
        OutputReportCollector.add("");

        testFiles.forEach(OutputReportCollector::add);
        OutputReportCollector.add("");
    }

    /**
     */
    public static TaskParameter buildTaskParameter(String hys, String ads, String tests, String moduleName) {
        String userDir = System.getProperty("user.dir");
        String pythonPath = userDir + File.separator + "model" + File.separator + "python" + File.separator;
        String word2vecPath = userDir + File.separator + "model" + File.separator + "word2vec" + File.separator;

        // python文件路径
        String pythonCppExtractor = pythonPath + "CppExtractor.py";
        String pythonWordVec = pythonPath + "WordVec.py";
        String pythonCalcTokenVec = pythonPath + "CalcTokenVec.py";
        String pythonCalcSimilarity = pythonPath + "CalcSimilarity.py";

        //中间过程：生成词向量文件的路径
        String corpusFilepath = word2vecPath + "corpus.txt";
        String wordVecModelFilepath = word2vecPath + "word2vec.model";
        String apiVectorDictFilepath = word2vecPath + "apiVectorDict.txt";
        String classVectorDictFilepath = word2vecPath + "classVectorDict.txt";
        String classCommentVectorDictFilepath = word2vecPath + "classCommentVectorDict.txt";
        String methodCommentDictFilepath = word2vecPath + "methodCommentDict.txt";
        String methodParamReturnVectorDictFilepath = word2vecPath + "methodParamReturnVectorDict.txt";
        String outputFilepath = userDir + File.separator + "output" + File.separator + moduleName + File.separator;
        String dbFilepath = userDir + File.separator + "data.db";

        InputStream in = ClassLoader.getSystemResourceAsStream("task.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String sourceProjectFilepath = properties.getProperty("sourceProjectFilepath");
        String targetProjectFilepath = properties.getProperty("targetProjectFilepath");
        String sourceModuleFilepath = properties.getProperty("sourceModuleFilepath");
        String targetModuleFilepath = properties.getProperty("targetModuleFilepath");
//        String sourceFilepath

        TaskParameter parameter = TaskParameter.builder()
                .taskId(Integer.parseInt(properties.getProperty("taskId")))
                // todo
//                .sourceFilepath(properties.getProperty("sourceFilepath"))
//                .targetFilepath(properties.getProperty("targetFilepath"))
//                .targetSourceCodeFilepath(properties.getProperty("targetSourceCodeFilepath"))
                .sourceFilepath(ads)
                .targetFilepath(hys)
                .targetSourceCodeFilepath(tests)
                .filepathKey(properties.getProperty("filepathKey"))
                .outputFilepath(outputFilepath)
                .pythonBinPath(properties.getProperty("pythonBinPath"))
                .corpusFilepath(corpusFilepath)
                .wordVecModelFilepath(wordVecModelFilepath)
                .pythonCppExtractor(pythonCppExtractor)
                .pythonWordVec(pythonWordVec)
                .pythonCalcTokenVec(pythonCalcTokenVec)
                .pythonCalcSimilarity(pythonCalcSimilarity)
                .dbFilepath(dbFilepath)
                .apiVectorDictFilepath(apiVectorDictFilepath)
                .classVectorDictFilepath(classVectorDictFilepath)
                .classCommentVectorDictFilepath(classCommentVectorDictFilepath)
                .methodCommentVectorDictFilepath(methodCommentDictFilepath)
                .methodParamReturnVectorDictFilepath(methodParamReturnVectorDictFilepath)
                .apiWordListFilepath(properties.getProperty("apiWordListFilepath"))
                .build();
        return parameter;
    }


    public static TaskParameter buildTaskParameter(String sourceFilepath, String targetFilepath, String sourceTestFilepath) {
        String userDir = System.getProperty("user.dir");
        String pythonPath = userDir + File.separator + "model" + File.separator + "python" + File.separator;
        String word2vecPath = userDir + File.separator + "model" + File.separator + "word2vec" + File.separator;

        // python文件路径
        String pythonCppExtractor = pythonPath + "CppExtractor.py";
        String pythonWordVec = pythonPath + "WordVec.py";
        String pythonCalcTokenVec = pythonPath + "CalcTokenVec.py";
        String pythonCalcSimilarity = pythonPath + "CalcSimilarity.py";

        //中间过程：生成词向量文件的路径
        String corpusFilepath = word2vecPath + "corpus.txt";
        String wordVecModelFilepath = word2vecPath + "word2vec.model";
        String apiVectorDictFilepath = word2vecPath + "apiVectorDict.txt";
        String classVectorDictFilepath = word2vecPath + "classVectorDict.txt";
        String classCommentVectorDictFilepath = word2vecPath + "classCommentVectorDict.txt";
        String methodCommentDictFilepath = word2vecPath + "methodCommentDict.txt";
        String methodParamReturnVectorDictFilepath = word2vecPath + "methodParamReturnVectorDict.txt";

        String outputFilepath = userDir + File.separator + "output" + File.separator;
        String dbFilepath = userDir + File.separator + "data.db";

        InputStream in = ClassLoader.getSystemResourceAsStream("task.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TaskParameter parameter = TaskParameter.builder()
                .taskId(Integer.parseInt(properties.getProperty("taskId")))
                .sourceFilepath(properties.getProperty("sourceFilepath"))
                .targetFilepath(properties.getProperty("targetFilepath"))
                .targetSourceCodeFilepath(properties.getProperty("targetSourceCodeFilepath"))
                .sourceFilepath(sourceFilepath)
                .targetFilepath(targetFilepath)
                .targetSourceCodeFilepath(sourceTestFilepath)
                .filepathKey(properties.getProperty("filepathKey"))
                .outputFilepath(outputFilepath)
                .pythonBinPath(properties.getProperty("pythonBinPath"))
                .corpusFilepath(corpusFilepath)
                .wordVecModelFilepath(wordVecModelFilepath)
                .pythonCppExtractor(pythonCppExtractor)
                .pythonWordVec(pythonWordVec)
                .pythonCalcTokenVec(pythonCalcTokenVec)
                .pythonCalcSimilarity(pythonCalcSimilarity)
                .dbFilepath(dbFilepath)
                .apiVectorDictFilepath(apiVectorDictFilepath)
                .classVectorDictFilepath(classVectorDictFilepath)
                .classCommentVectorDictFilepath(classCommentVectorDictFilepath)
                .methodCommentVectorDictFilepath(methodCommentDictFilepath)
                .methodParamReturnVectorDictFilepath(methodParamReturnVectorDictFilepath)
                .apiWordListFilepath(properties.getProperty("apiWordListFilepath"))
                .build();
        return parameter;
    }



    public static TaskParameter buildTaskParameter() {
        String userDir = System.getProperty("user.dir");
        String pythonPath = userDir + File.separator + "model" + File.separator + "python" + File.separator;
        String word2vecPath = userDir + File.separator + "model" + File.separator + "word2vec" + File.separator;

        String pythonCppExtractor = pythonPath + "CppExtractor.py";
        String pythonWordVec = pythonPath + "WordVec.py";
        String pythonCalcTokenVec = pythonPath + "CalcTokenVec.py";
        String pythonCalcSimilarity = pythonPath + "CalcSimilarity.py";

        String corpusFilepath = word2vecPath + "corpus.txt";
        String wordVecModelFilepath = word2vecPath + "word2vec.model";
        String apiVectorDictFilepath = word2vecPath + "apiVectorDict.txt";
        String classVectorDictFilepath = word2vecPath + "classVectorDict.txt";
        String classCommentVectorDictFilepath = word2vecPath + "classCommentVectorDict.txt";
        String methodCommentDictFilepath = word2vecPath + "methodCommentDict.txt";
        String methodParamReturnVectorDictFilepath = word2vecPath + "methodParamReturnVectorDict.txt";

        String outputFilepath = userDir + File.separator + "output" + File.separator;
        String dbFilepath = userDir + File.separator + "data.db";

        InputStream in = ClassLoader.getSystemResourceAsStream("task.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return TaskParameter.builder()
                .taskId(Integer.parseInt(properties.getProperty("taskId")))
                .sourceFilepath(properties.getProperty("sourceFilepath"))
                .targetFilepath(properties.getProperty("targetFilepath"))
                .targetSourceCodeFilepath(properties.getProperty("targetSourceCodeFilepath"))
                .filepathKey(properties.getProperty("filepathKey"))
                .outputFilepath(outputFilepath)
                .pythonBinPath(properties.getProperty("pythonBinPath"))
                .corpusFilepath(corpusFilepath)
                .wordVecModelFilepath(wordVecModelFilepath)
                .pythonCppExtractor(pythonCppExtractor)
                .pythonWordVec(pythonWordVec)
                .pythonCalcTokenVec(pythonCalcTokenVec)
                .pythonCalcSimilarity(pythonCalcSimilarity)
                .dbFilepath(dbFilepath)
                .apiVectorDictFilepath(apiVectorDictFilepath)
                .classVectorDictFilepath(classVectorDictFilepath)
                .classCommentVectorDictFilepath(classCommentVectorDictFilepath)
                .methodCommentVectorDictFilepath(methodCommentDictFilepath)
                .methodParamReturnVectorDictFilepath(methodParamReturnVectorDictFilepath)
                .apiWordListFilepath(properties.getProperty("apiWordListFilepath"))
                .build();

    }


}
