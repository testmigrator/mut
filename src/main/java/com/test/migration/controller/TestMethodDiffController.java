package com.test.migration.controller;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.entity.Constants;
import com.test.migration.entity.enums.TestMethodStatus;
import com.test.migration.entity.po.TestMethodDiff;
import com.test.migration.entity.response.CommonReturnType;
import com.test.migration.entity.vo.CodeDiffVO;
import com.test.migration.service.TestMethodDiffService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import utils.FileReadUtil;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/testMethodDiff")
public class TestMethodDiffController {

    @Resource
    private TestMethodDiffService testMethodDiffService;

    @GetMapping(value = "/list")
    public CommonReturnType listAllTestMethodDiffs() throws IOException {
        List<TestMethodDiff> list = testMethodDiffService.list().stream()
                .filter(x -> StringUtils.equals(x.getType(), Constants.CodeDiffType.TestMethod))
//                .filter(x -> !StringUtils.equals(x.getStatus(), "delete"))
                .collect(Collectors.toList());
        TestMethodDiff testMethodDiff = list.stream().findFirst().orElse(null);
        if (testMethodDiff == null) {
            return CommonReturnType.create(true);
        }

        // 人工调整api mapping 并筛选出待迁移test
//        TODO
//        list = filterReList(list);

//        String[] fileSplit = testMethodDiff.getFilepath().split(File.separator);
//        String filename = fileSplit[fileSplit.length - 1].toLowerCase();

        CodeDiffVO.SetupTearDown setupTearDown = buildSetupTearDown(testMethodDiff.getFilepath());
        CodeDiffVO.ClassAndField classAndField = buildClassAndField(testMethodDiff.getFilepath());

        List<CodeDiffVO> codeDiffVOS = list.stream()
                .map(x -> CodeDiffVO.builder()
                        .methodName(x.getMethodName())
                        .filepath(x.getFilepath())
                        // 暂时没用
                        .filename("")
                        .businessKey(x.getBusinessKey())
                        .type(x.getType())
                        .status(TestMethodStatus.codeOf(x.getStatus()))
                        .oldCode(x.getOldCode())
                        .newCode(x.getNewCode())
                        .classMappingTips(x.getClassMappingTips())
                        .missMappingTips(x.getMissMappingTips())
                        .testMethodDesc(x.getTestMethodDesc())
                        .testComponentDesc(x.getTestComponentDesc())
                        .decoupleOldCode(x.getDecoupleOldCode())
                        .decoupleNewCode(x.getDecoupleNewCode())
                        .setupTearDown(setupTearDown)
                        .classAndField(classAndField)
                        .build())
                .collect(Collectors.toList());
        return CommonReturnType.create(codeDiffVOS);
    }

    private static List<TestMethodDiff> filterReList(List<TestMethodDiff> list) throws IOException {
        String testReFilepath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/experiment/tmp/test_invoke_Re.txt";
        List<String> testRes = FileReadUtil.readFile(testReFilepath);
        List<String> testResList = testRes.stream()
                .filter(x -> x.contains("$"))
                .collect(Collectors.toList());

        List<TestMethodDiff> filterList = list.stream()
                .filter(x -> testResList.contains(x.getFilepath() + "$" + x.getMethodName()))
                .collect(Collectors.toList());

        if(filterList.isEmpty()){
            return list;
        }

        return filterList;
    }

    @PostMapping(value = "/updateStatus")
    public CommonReturnType updateStatus(@RequestParam("businessKey") String businessKey,
                                         @RequestParam("status") String status) {

        TestMethodDiff testMethodDiff = TestMethodDiff.builder()
                .businessKey(businessKey)
                .status(status)
                .build();

        testMethodDiffService.update(testMethodDiff);
        return CommonReturnType.create(TestMethodStatus.codeOf(status));
    }

    @PostMapping(value = "/migrateDecoupleCode")
    public CommonReturnType migrateDecoupleCode(@RequestParam("businessKey") String businessKey) {
        testMethodDiffService.translateCoupleCode(businessKey);
        return CommonReturnType.create(true);
    }

    @PostMapping(value = "/updateDesc")
    public CommonReturnType updateDesc(@RequestParam("businessKey") String businessKey,
                                       @RequestParam("testMethodDesc") String testMethodDesc,
                                       @RequestParam("testComponentDesc") String testComponentDesc,
                                       @RequestParam("decoupleCode") String decoupleCode) {

        TestMethodDiff testMethodDiff = TestMethodDiff.builder()
                .businessKey(businessKey)
                .build();

        if (StringUtils.isNotBlank(testMethodDesc)) {
            testMethodDiff.setTestMethodDesc(testMethodDesc);
        }
        if (StringUtils.isNotBlank(testComponentDesc)) {
            testMethodDiff.setTestComponentDesc(testComponentDesc);
        }
        if (StringUtils.isNotBlank(decoupleCode)) {
            String formatDecoupleCode = "public class Test{\n " + decoupleCode + "}";
            testMethodDiff.setDecoupleOldCode(formatDecoupleCode);
        }

        testMethodDiffService.update(testMethodDiff);
        return CommonReturnType.create(true);
    }

    private CodeDiffVO.ClassAndField buildClassAndField(String filepath) {
        List<TestMethodDiff> resList = Lists.newArrayList();
        List<TestMethodDiff> classList = testMethodDiffService.list().stream()
                .filter(x -> StringUtils.equals(x.getFilepath(), filepath))
                .filter(x -> StringUtils.equals(x.getType(), Constants.CodeDiffType.ClassDeclaration))
//                .filter(x -> !StringUtils.equals(x.getStatus(), "delete"))
                .collect(Collectors.toList());
        resList.addAll(classList);

        List<TestMethodDiff> fieldList = testMethodDiffService.list().stream()
                .filter(x -> StringUtils.equals(x.getFilepath(), filepath))
                .filter(x -> StringUtils.equals(x.getType(), Constants.CodeDiffType.FieldDeclaration))
//                .filter(x -> !StringUtils.equals(x.getStatus(), "delete"))
                .collect(Collectors.toList());
        resList.addAll(fieldList);

        TestMethodDiff testMethodDiff = resList.stream().findFirst().orElse(null);
        if (testMethodDiff == null) {
            CodeDiffVO.ClassAndField defaultDiff = new CodeDiffVO.ClassAndField();
            defaultDiff.setOldCode("");
            defaultDiff.setNewCode("");
            return defaultDiff;
        }

        List<String> oldCodeList = resList.stream()
                .map(TestMethodDiff::getOldCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String oldCode = CollectionUtils.isEmpty(oldCodeList) ? StringUtils.EMPTY : Joiner.on("\n").join(oldCodeList);

        List<String> newCodeList = resList.stream()
                .map(TestMethodDiff::getNewCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String newCode = CollectionUtils.isEmpty(newCodeList) ? StringUtils.EMPTY : Joiner.on("\n").join(newCodeList);

        List<String> classMappingTipsList = resList.stream()
                .map(TestMethodDiff::getClassMappingTips)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String classMappingTips = CollectionUtils.isEmpty(classMappingTipsList) ? StringUtils.EMPTY : Joiner.on("\n").join(classMappingTipsList);

        List<String> missMappingTipsList = resList.stream()
                .map(TestMethodDiff::getMissMappingTips)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String missMappingTips = CollectionUtils.isEmpty(missMappingTipsList) ? StringUtils.EMPTY : Joiner.on("\n").join(missMappingTipsList);


        return CodeDiffVO.ClassAndField.builder()
                .oldCode(oldCode)
                .newCode(newCode)
                .classMappingTips(classMappingTips)
                .missMappingTips(missMappingTips)
                .build();
    }

    /**
     * setup/teardown
     */
    public CodeDiffVO.SetupTearDown buildSetupTearDown(String filepath) {
        List<TestMethodDiff> resList = testMethodDiffService.list().stream()
                .filter(x -> StringUtils.equals(x.getFilepath(), filepath))
                .filter(x -> StringUtils.equals(x.getType(), Constants.CodeDiffType.SetupTearDown))
                .collect(Collectors.toList());

        TestMethodDiff testMethodDiff = resList.stream().findFirst().orElse(null);
        if (testMethodDiff == null) {
            CodeDiffVO.SetupTearDown defaultDiff = new CodeDiffVO.SetupTearDown();
            defaultDiff.setOldCode("");
            defaultDiff.setNewCode("");
            return defaultDiff;
        }
        List<String> oldCodeList = resList.stream()
                .map(TestMethodDiff::getOldCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String oldCode = CollectionUtils.isEmpty(oldCodeList) ? StringUtils.EMPTY : Joiner.on("\n").join(oldCodeList);

        List<String> newCodeList = resList.stream()
                .map(TestMethodDiff::getNewCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String newCode = CollectionUtils.isEmpty(newCodeList) ? StringUtils.EMPTY : Joiner.on("\n").join(newCodeList);

        List<String> classMappingTipsList = resList.stream()
                .map(TestMethodDiff::getClassMappingTips)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String classMappingTips = CollectionUtils.isEmpty(classMappingTipsList) ? StringUtils.EMPTY : Joiner.on("\n").join(classMappingTipsList);

        List<String> missMappingTipsList = resList.stream()
                .map(TestMethodDiff::getMissMappingTips)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String missMappingTips = CollectionUtils.isEmpty(missMappingTipsList) ? StringUtils.EMPTY : Joiner.on("\n").join(missMappingTipsList);

        return CodeDiffVO.SetupTearDown.builder()
                .oldCode(oldCode)
                .newCode(newCode)
                .classMappingTips(classMappingTips)
                .missMappingTips(missMappingTips)
                .build();
    }
}
