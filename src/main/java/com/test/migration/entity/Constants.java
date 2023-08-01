package com.test.migration.entity;

import com.google.common.collect.Lists;

import java.util.List;

public interface Constants {
    String tmpFilter = "/Users/gaoyi/IdeaProjects/TestMigrationV2/src/main/resources/tmpFilter.txt";


    interface API_BASIC_TASK {
        String SOURCE = "Source";
        String TARGET = "Target";
        int FILE_PARSE_TIME_OUT = 15;
        String TARGET_API_SEPARATOR = "$$$";
    }

    interface MAPPING_TASK{

    }


    interface API_TYPE {
        int SOURCE_API = 0;
        int SOURCE_NON_API = 3;
        int TARGET_API = 1;
        int TARGET_NON_API = 4;
    }

    interface FILE_TYPE {
        String JAVA = "java";
        String CPP_HEAD_H = "h";
        String CPP_HEAD_HPP = "hpp";

        String CPP = "cpp";
    }


    interface SEPARATOR {
        String COMMA = ",";
        String SEMI = ";";
        String SPACE = " ";
        String BAR = "-";
        String DOT = ".";
        String ARROW = "->";
        String POUND = "#";
    }

    interface NON_API_FILTER {
        List<String> NON_APIS = Lists.newArrayList(
                "set",
                "get",
                "tostring"
        );
    }

    String TEST = "test";
    String TESTS = "tests";

    /**
     * 出现类匹配（sourceClass和targetClass）一对多匹配关系时，多个target之间用分隔符连接
     */
    String ClassMappingOneToManySplitter = "-OTM-";


    interface Hint {
        /**
         * 存在一对多类匹配关系，如果出现在方法内部，会根据api调用关系进行target筛选，如果是在外部，那么会把所有的target以注释形式列举出来
         */
        String multipleClassMappingMessage = "// There are multiple target class mapping references: \n";

        String missingMappingMessage = "// There are no mapping references for the following components: \n";
    }

    interface CodeDiffType {
        String TestMethod = "TestMethod";
        String OtherMethod = "OtherMethod";
        String SetupTearDown = "SetupTearDown";
        String ClassDeclaration = "ClassDeclaration";
        String FieldDeclaration = "FieldDeclaration";
    }
}
