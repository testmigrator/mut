package com.test.migration;

import com.test.migration.service.MigrateTestService;
import utils.MappingRuleWriter;
import utils.MyBatisUtil;

public class MigrateTestMain {
    // 生成api关联test信息
    MigrateTestService migrateTestService = new MigrateTestService();

    public static void main(String[] args) {
        MigrateTestMain main = new MigrateTestMain();
        main.process();
    }

    public void process() {
        // 筛选出待迁移的test
        migrateTestService.generateMigrateTest();
    }

}
