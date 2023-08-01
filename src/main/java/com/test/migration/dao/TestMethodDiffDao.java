package com.test.migration.dao;

import com.test.migration.entity.po.TestMethodDiff;

import java.util.List;

public interface TestMethodDiffDao {
    void dropTable();
    void createTable();
    void insert(TestMethodDiff testMethodDiff);
    void update(TestMethodDiff testMethodDiff);
    TestMethodDiff getById(Integer id);
    TestMethodDiff getByBusinessKey(String businessKey);
    List<TestMethodDiff> selectAll();
    List<TestMethodDiff> selectByIds(List<Integer> ids);
}
