package utils;

import com.test.migration.dao.ApiBasicDao;
import com.test.migration.dao.ApiMappingDao;
import com.test.migration.dao.TestMethodDiffDao;
import com.test.migration.dao.MigrateTestDao;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MyBatisUtil {

    private static SqlSessionFactory factory = null;

    static {
        try {
            String resource = "mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            factory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SqlSession getSqlSession() throws IOException {
        return factory.openSession(true);
    }

    public static void initTable() {
        dropTable();
        createTable();
    }

    private static void dropTable() {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiBasicDao apiBasicMapper = session.getMapper(ApiBasicDao.class);
            apiBasicMapper.dropTable();

            ApiMappingDao apiMappingMapper = session.getMapper(ApiMappingDao.class);
            apiMappingMapper.dropTable();

            MigrateTestDao translateTestMapper = session.getMapper(MigrateTestDao.class);
            translateTestMapper.dropTable();

            TestMethodDiffDao testMethodDiffMapper = session.getMapper(TestMethodDiffDao.class);
            testMethodDiffMapper.dropTable();
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTable() {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ApiBasicDao apiBasicMapper = session.getMapper(ApiBasicDao.class);
            apiBasicMapper.createTable();

            ApiMappingDao apiMappingMapper = session.getMapper(ApiMappingDao.class);
            apiMappingMapper.createTable();

            MigrateTestDao translateTestMapper = session.getMapper(MigrateTestDao.class);
            translateTestMapper.createTable();

            TestMethodDiffDao testMethodDiffMapper = session.getMapper(TestMethodDiffDao.class);
            testMethodDiffMapper.createTable();
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
