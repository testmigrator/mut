package com.test.migration.service;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.dao.ApiBasicDao;
import com.test.migration.entity.Constants;
import com.test.migration.entity.TaskParameter;
import com.test.migration.entity.po.ApiBasic;
import com.test.migration.service.extract.APIExtractorVisitor;
import com.test.migration.service.preprocess.CommonBasicService;
import com.test.migration.service.preprocess.Preprocess;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Service
public class ApiBasicService {

    /**
     */
    public void extractApiBasic(List<String> sourceFileTypes, List<String> targetFileTypes) {
        long startTime = System.currentTimeMillis();

        try {
            TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

            doExtract(taskParameter.getSourceFilepath(), sourceFileTypes, Constants.API_BASIC_TASK.SOURCE);

            doExtract(taskParameter.getTargetFilepath(), targetFileTypes, Constants.API_BASIC_TASK.TARGET);
        } catch (Exception e) {
            Log.error("extract api error");
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
    }

    /**
     */
    private void doExtract(String filepath, List<String> fileTypes, String parseType) {
        long startTime = System.currentTimeMillis();

        List<String> apiFilepathList = fileTypes.stream()
                .flatMap(fileType -> filterApiFilepathByFileType(filepath, fileType).stream())
                .collect(Collectors.toList());

        // todo tmp filter
//        apiFilepathList = TestApiMain.tmpFilter(apiFilepathList, parseType);

        for (String fileType : fileTypes) {
            apiFilepathList = filterFileByFilename(apiFilepathList, fileType);
        }

        parseAndSaveApiBasic(apiFilepathList, parseType);

        long endTime = System.currentTimeMillis();
    }



    private List<String> filterApiFilepathByFileType(String filePaths, String fileType) {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<String> filepathList = Splitter.on(Constants.SEPARATOR.COMMA)
                .splitToList(filePaths);

        return filepathList.stream()
                .flatMap(subFilepath -> {
                    List<String> files = GetFoldFileNames.readfileWithType(subFilepath, fileType);
                    return files.stream().filter(file ->
                            filterByFilepathKey(taskParameter.getFilepathKey(), file));
                })
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * @param filepathKey
     * @param file
     * @return
     */
    private boolean filterByFilepathKey(String filepathKey, String file) {
        if (StringUtils.isNotBlank(filepathKey)) {
            String[] fileSplit = file.split(File.separator);
            String filename = fileSplit[fileSplit.length - 1].toLowerCase();
            return !file.contains(Constants.TEST) && filename.contains(filepathKey);
        }
        return true;
    }

    private void parseAndSaveApiBasic(List<String> apiFilepathList, String parseType) {
        apiFilepathList = CommonBasicService.filterTestFilepath(apiFilepathList);

        List<ApiBasic> apiBasics = apiFilepathList.parallelStream()
                .flatMap(filepath -> {
                    if (StringUtils.equals(parseType, Constants.API_BASIC_TASK.SOURCE)) {
                        return parseSourceApiBasic(filepath).stream();
                    }
                    if (StringUtils.equals(parseType, Constants.API_BASIC_TASK.TARGET)) {
                        return parseTargetApiBasicTimeout(filepath).stream();
                    }
                    return null;
                })
                .collect(Collectors.toList());
        for (ApiBasic apiBasic : apiBasics) {
            setNonApiType(apiBasic, parseType);
        }

        batchSave(apiBasics);

    }

    private List<ApiBasic> parseSourceApiBasic(String filePath) {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(new Java8Lexer(inputStream));
        Java8Parser parser = new Java8Parser(commonTokenStream);
        ParseTree parseTree = parser.compilationUnit();

        List<Token> tokens = fetchCommentTokens(filePath);

        APIExtractorVisitor apiExtractor = new APIExtractorVisitor(taskParameter.getTaskId(), filePath);
        apiExtractor.visit(parseTree);
        apiExtractor.fillCodeComment(tokens);

        return buildSourceApiBasic(taskParameter.getTaskId(), filePath, apiExtractor.getMethodModelList());
    }

    private List<ApiBasic> parseTargetApiBasicTimeout(String filepath) {
        List<ApiBasic> apiBasics = Lists.newArrayList();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> task = () -> {
            apiBasics.addAll(parseTargetApiBasic(filepath));
            return StringUtils.EMPTY;
        };

        Future<String> future = executor.submit(task);
        try {
            future.get(Constants.API_BASIC_TASK.FILE_PARSE_TIME_OUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            Log.error("Task cancelled due to timeout");
        } catch (Exception e) {
            Log.error("parseTargetApiBasicTimeout error");
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        return apiBasics;
    }

    public List<ApiBasic> parseTargetApiBasic(String filepath) {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        String[] args = new String[]{taskParameter.getPythonBinPath(), taskParameter.getPythonCppExtractor(), filepath};

        List<String> resultLines = CallUtil.call(args);
        return buildTargetApiBasic(taskParameter.getTaskId(), filepath, resultLines);
    }

    private List<ApiBasic> buildSourceApiBasic(Integer taskId, String filepath, List<APIExtractorVisitor.MethodModel> methodModelList) {
        String className = CommonBasicService.getClassNameByFilepath(filepath);
        return methodModelList.stream()
                .map(methodModel -> ApiBasic.builder()
                        .taskId(taskId)
                        .filepath(filepath)
                        .className(className)
                        .apiName(methodModel.getMethodName())
                        .type(Constants.API_TYPE.SOURCE_API)
                        .methodWordSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.generateWordSequence(methodModel.getMethodName())))
                        .tokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocess(methodModel.getMethodName())))
                        .classNameTokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocess(className)))
                        .classCommentTokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocessPhrase(methodModel.getClassComment())))
                        .methodCommentTokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocessPhrase(methodModel.getMethodComment())))
                        .methodParamReturnTokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocess(methodModel.getMethodParameter() + methodModel.getMethodReturn())))
                        .build()
                ).collect(Collectors.toList());
    }

    private List<ApiBasic> buildTargetApiBasic(Integer taskId, String filepath, List<String> resultLines) {
        if (resultLines.isEmpty()) {
            return Lists.newArrayList();
        }

        String apiLines = resultLines.stream()
                .filter(apiLine -> apiLine.contains(Constants.API_BASIC_TASK.TARGET_API_SEPARATOR))
                .findFirst().orElse(StringUtils.EMPTY);

        if (StringUtils.isBlank(apiLines)) {
            return Lists.newArrayList();
        }

        List<String> apis = JsonUtil.jsonToList(apiLines, String.class);

        return apis.stream().map(line -> {
                    List<String> toList = Splitter.on(Constants.API_BASIC_TASK.TARGET_API_SEPARATOR).splitToList(line);
                    if (toList.size() < 6) {
                        return null;
                    }
                    String classComment = toList.get(0);
                    String className = toList.get(1);
                    String methodComment = toList.get(2);
                    String methodName = toList.get(3);
                    String methodParamReturn = toList.get(4) + toList.get(5);

                    return ApiBasic.builder()
                            .taskId(taskId)
                            .filepath(filepath)
                            .className(className)
                            .apiName(methodName)
                            .type(Constants.API_TYPE.TARGET_API)
                            .methodWordSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.generateWordSequence(methodName)))
                            .tokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocess(methodName)))
                            .classNameTokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocess(className)))
                            .classCommentTokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocessPhrase(classComment)))
                            .methodCommentTokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocessPhrase(methodComment)))
                            .methodParamReturnTokenSequence(Joiner.on(Constants.SEPARATOR.COMMA).join(Preprocess.preprocess(methodParamReturn)))
                            .build();
                })
                .filter(Objects::nonNull).collect(Collectors.toList());

    }


    private List<Token> fetchCommentTokens(String filepath) {
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CommonTokenStream commonTokenStream = new CommonTokenStream(new com.test.migration.antlr.java.comment.Java8Lexer(inputStream));
        Java8Parser parser = new Java8Parser(commonTokenStream);
        parser.compilationUnit();
        return commonTokenStream.getTokens();
    }


    private void setNonApiType(ApiBasic apiBasic, String parseType) {
        boolean isApi = CommonBasicService.filterNonApi(apiBasic.getApiName(), apiBasic.getClassName());
        if (isApi) {
            return;
        }

        if (StringUtils.equals(parseType, Constants.API_BASIC_TASK.SOURCE)) {
            apiBasic.setType(Constants.API_TYPE.SOURCE_NON_API);
        }

        if (StringUtils.equals(parseType, Constants.API_BASIC_TASK.TARGET)) {
            apiBasic.setType(Constants.API_TYPE.TARGET_NON_API);
        }
    }


    private List<String> filterFileByFilename(List<String> apiFilepathList, String fileType) {
        String fileTypeComma = Constants.SEPARATOR.DOT + fileType;
        try {
            String tmpFilepath = Constants.tmpFilter;
            List<String> filterFileNameList = FileReadUtil.readFile(tmpFilepath);
            filterFileNameList = filterFileNameList.stream()
                    .filter(x -> x.contains(fileTypeComma))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filterFileNameList)) {
                return apiFilepathList;
            }

            List<String> finalFilterFileNameList = filterFileNameList;
            apiFilepathList = apiFilepathList.stream()
                    .filter(x -> {
                        String filename = CommonBasicService.getClassNameByFilepath(x);
                        return finalFilterFileNameList.contains(filename + fileTypeComma);
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            Log.error(" filterFileByFilename error");
            e.printStackTrace();
        }

        return apiFilepathList;
    }

    /**
     * CRUD
     **/
    public List<ApiBasic> selectByTaskId(Integer taskId) {
        List<ApiBasic> list = Lists.newArrayList();

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiBasicDao mapper = session.getMapper(ApiBasicDao.class);
            list = mapper.selectByTaskId(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<ApiBasic> selectByIds(List<Integer> ids) {
        List<ApiBasic> list = Lists.newArrayList();

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiBasicDao mapper = session.getMapper(ApiBasicDao.class);
            list = mapper.selectByIds(ids);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void batchSave(List<ApiBasic> apiBasics) {
        if (CollectionUtils.isEmpty(apiBasics)) {
            return;
        }

        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiBasicDao mapper = session.getMapper(ApiBasicDao.class);
            mapper.batchInsert(apiBasics);
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteById(Integer id) {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiBasicDao mapper = session.getMapper(ApiBasicDao.class);
            mapper.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
