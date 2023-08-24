package com.test.migration;

import com.test.migration.service.MigrateTestService;
import utils.MappingRuleWriter;
import utils.MyBatisUtil;

public class MigrateTestMain {
    MigrateTestService migrateTestService = new MigrateTestService();

    public static void main(String[] args) {
        MigrateTestMain main = new MigrateTestMain();
        main.process();
    }

    public void process() {
        migrateTestService.generateMigrateTest();
    }

}
