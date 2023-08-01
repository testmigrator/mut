package main;

import com.test.migration.entity.po.TestMethodDiff;
import com.test.migration.service.TestMethodDiffService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestFindMethodDiff {
    static TestMethodDiffService testMethodDiffService = new TestMethodDiffService();

    public static void main(String[] args) {
        //AccessibilityCacheTest
        String filepath = "/Users/gaoyi/IdeaProjects/staticCodeAnalysis/doc/android/source/platform_frameworks_base/core/tests/coretests/src/android/view/accessibility/AccessibilityCacheTest.java";
        List<TestMethodDiff> list = testMethodDiffService.list();
        list = list.stream().filter(x -> StringUtils.equals(x.getFilepath(), filepath))
                .collect(Collectors.toList());
        list.forEach(x -> System.out.println(x));
    }
}
