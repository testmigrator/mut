package com.test.migration.dao;

import com.test.migration.entity.po.MigrateTest;

import java.util.List;

public interface MigrateTestDao {
    void dropTable();
    void createTable();
    void batchInsert(List<MigrateTest> migrateTests);
    List<MigrateTest> selectByTaskId(Integer taskId);

}
