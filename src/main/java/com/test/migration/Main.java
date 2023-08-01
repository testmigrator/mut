package com.test.migration;

public class Main {
    static ApiMappingMain apiMappingMain = new ApiMappingMain();
    static MigrateTestMain migrateTestMain = new MigrateTestMain();
    static TranslateMain translateMain = new TranslateMain();

    public static void main(String[] args) {
        apiMappingMain.process();

        migrateTestMain.process();

        translateMain.process();
    }
}
