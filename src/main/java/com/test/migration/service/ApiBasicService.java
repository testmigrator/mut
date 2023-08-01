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
import com.test.migration.tmp.TestApiMain;
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
     * 提取api基础信息
     */
    public void extractApiBasic(List<String> sourceFileTypes, List<String> targetFileTypes) {
        Log.info("开始获取api基础信息");
        long startTime = System.currentTimeMillis();

        try {
            TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

            // 在source java文件中提取api基础信息
            doExtract(taskParameter.getSourceFilepath(), sourceFileTypes, Constants.API_BASIC_TASK.SOURCE);

            // 在target cpp文件中提取api基础信息
            doExtract(taskParameter.getTargetFilepath(), targetFileTypes, Constants.API_BASIC_TASK.TARGET);
        } catch (Exception e) {
            Log.error("extract api error");
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        Log.info("获取api基础信息完成，耗时（毫秒）：" + (endTime - startTime));
    }

    /**
     * 执行api信息提取
     *
     * @param filepath  文件目录
     * @param fileTypes java/h/hpp
     * @param parseType source/target
     */
    private void doExtract(String filepath, List<String> fileTypes, String parseType) {
        Log.info("开始提取[" + parseType + "]项目API信息");
        long startTime = System.currentTimeMillis();

        // 筛选出api所在的文件
        List<String> apiFilepathList = fileTypes.stream()
                .flatMap(fileType -> filterApiFilepathByFileType(filepath, fileType).stream())
                .collect(Collectors.toList());

        // todo tmp filter
//        apiFilepathList = TestApiMain.tmpFilter(apiFilepathList, parseType);

        // 过滤出指定文件
        for (String fileType : fileTypes) {
            apiFilepathList = filterFileByFilename(apiFilepathList, fileType);
        }

        // 解析文件并保存提取到的api信息
        parseAndSaveApiBasic(apiFilepathList, parseType);

        long endTime = System.currentTimeMillis();
        Log.info("[" + parseType + "]项目API信息提取完成, 文件数目：" + apiFilepathList.size()
                + ", 耗时[毫秒]:" + (endTime - startTime));
    }


    /**
     * 按照指定文件类型来筛选待解析文件
     */
    private List<String> filterApiFilepathByFileType(String filePaths, String fileType) {
        TaskParameter taskParameter = TaskParameterReader.getTaskParameter();

        List<String> filepathList = Splitter.on(Constants.SEPARATOR.COMMA)
                .splitToList(filePaths);

        // 读取指定文件类型的文件，如果指定了key，则再按key进行过滤
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
     * 如果指定了模块（key），则只提取和解析包含该key的文件
     * 如果没指定则默认全部提取和解析
     *
     * @param filepathKey 模块key
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
        // 过滤掉test文件
        apiFilepathList = CommonBasicService.filterTestFilepath(apiFilepathList);

        // 定制解析
        List<ApiBasic> apiBasics = apiFilepathList.parallelStream()
                .flatMap(filepath -> {
                    //解析source文件
                    if (StringUtils.equals(parseType, Constants.API_BASIC_TASK.SOURCE)) {
                        return parseSourceApiBasic(filepath).stream();
                    }
                    //解析target文件
                    if (StringUtils.equals(parseType, Constants.API_BASIC_TASK.TARGET)) {
                        return parseTargetApiBasicTimeout(filepath).stream();
                    }
                    return null;
                })
                .collect(Collectors.toList());

        // 如果不是普通api(如get/set)，则设置对应的api类型
        for (ApiBasic apiBasic : apiBasics) {
            setNonApiType(apiBasic, parseType);
        }

        // 保存api到数据库
        batchSave(apiBasics);

        Log.info("[" + parseType + "] 共计提取API总数：" + apiBasics.size());
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
        // 填充代码注释信息
        apiExtractor.fillCodeComment(tokens);

        return buildSourceApiBasic(taskParameter.getTaskId(), filePath, apiExtractor.getMethodModelList());
    }

    private List<ApiBasic> parseTargetApiBasicTimeout(String filepath) {
        List<ApiBasic> apiBasics = Lists.newArrayList();

        // 文件解析超时就跳过该文件
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> task = () -> {
            // 执行长时间任务的代码
            apiBasics.addAll(parseTargetApiBasic(filepath));
            return StringUtils.EMPTY;
        };

        Future<String> future = executor.submit(task);
        try {
            // 文件解析超时设置：15秒
            future.get(Constants.API_BASIC_TASK.FILE_PARSE_TIME_OUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // 如果方法执行超时，取消该文件解析任务
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

        Log.info("正在提取目标项目文件:" + filepath);
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

    /**
     * antlr版本冲突问题（4.7和4.10.1）
     */
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

    /**
     * 如果不是普通api(如get/set)，则设置对应的api类型
     */
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


    /**
     * 如果tmpFilter文件中指定了类名列表，则只解析这些文件（按类型：java/h/hpp）
     * 如果没有指定，则默认全部解析
     * TODO：tmp方法
     */
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
