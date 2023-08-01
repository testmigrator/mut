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

    // 使用static静态代码块，随着类的加载而加载，只执行一次
    static {
        try {
            String resource = "mybatis/mybatis-config.xml";
            // 加载MyBatis的主配置文件
            InputStream inputStream = Resources.getResourceAsStream(resource);
            // 通过构建器（SqlSessionFactoryBuilder）构建一个SqlSessionFactory工厂对象
            factory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SqlSession getSqlSession() throws IOException {
        // true ：自动提交事务
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
