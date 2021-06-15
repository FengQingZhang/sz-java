package com.zzgk.sys.dao.current;

import com.zzgk.sys.entity.current.IPagination;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.*;
import java.util.List;
import java.util.Map;

@Repository
public class MyRepositoryImpl implements MyRepository{

    @Autowired
    DataSource dataSource;
    @Autowired
    EntityManager entityManager;


    /**
     * 创建表格
     *
     * @param sql sql语句
     */
    @Override
    public void createTable(String sql) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            close(connection, statement, null);
        }
    }

    /**
     * 更新数据
     *
     * @param sql  sql语句
     * @param maps 参数和value的map
     * @return int
     */
    @Override
    @Transactional
    public int updateData(String sql, Map<String, Object> maps) {
        Query query = getQueryWithParameters(entityManager.createNativeQuery(sql), maps);
        return query.executeUpdate();
    }

    /**
     * 表格是否存在
     *
     * @param tableName 表格名称
     * @return boolean
     */
    @Override
    public boolean isTableExist(String tableName) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            DatabaseMetaData data = conn.getMetaData();
            String[] types = {"TABLE"};
            //conn.getCatalog()获取当前数据库的名称
            String catalog = conn.getCatalog();
            rs = data.getTables(catalog, null, tableName, types);
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            close(conn, null, rs);
        }
        return false;
    }

    /**
     * 简单的分页搜索
     *
     * @param tableName 表名
     * @param where     where语句部分
     * @param map       参数名和value的map
     * @param page      页码
     * @param size      每页数据量
     * @return IPagination<Map<String,Object>>
     */
    @Override
    public IPagination<Map<String,Object>> searchReturnIPagination(String tableName, String where, Map<String, Object> map, int page, int size) {
        StringBuilder countSql = new StringBuilder("SELECT count(1) FROM " + tableName + "  where 1=1 ");
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " where 1=1 ");
        countSql.append(where);
        sql.append(where);
        return search(page, size, countSql.toString(), sql.toString(), map);
    }

    /**
     *
     * 自定义sql语句的分页搜索
     * @param page     页码
     * @param size     每页数量
     * @param countSql 查数量sql
     * @param sql      查询sql
     * @param maps     参数和value的map
     * @return IPagination<Map<String,Object>>
     */
    @Override
    public IPagination<Map<String,Object>> search(int page, int size, String countSql, String sql, Map<String, Object> maps) {
        // 初始化分页实体类
        IPagination<Map<String,Object>> pagination = IPagination.create(page, size);
        // 查询结果总条数
        Query cQuery = getQueryWithParameters(entityManager.createNativeQuery(countSql), maps);
        List rows = cQuery.getResultList();
        int total = Integer.parseInt(rows.get(0).toString());
        // getQueryWithParameters(entityManager.createNativeQuery(sql),
        // maps).getResultList().size();
        pagination.setTotal(total);
        if (total == 0)
            return pagination;
        Query query = getQueryWithParameters(entityManager.createNativeQuery(sql), maps);
        // 设置返回一页数据
        query.setFirstResult((page - 1) * size).setMaxResults(pagination.getSize());
        // 指定返回对象类型
        query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        // 数据列表
        pagination.setList(query.getResultList());
        return pagination;
    }

    /**
     * 创建参数映射
     *
     * @param query query
     * @param maps  参数和值的map
     * @return Query
     */
    @Override
    public Query getQueryWithParameters(Query query, Map<String, Object> maps) {
        if (maps.size() > 0) {
            for (String key : maps.keySet()) {
                query.setParameter(key, maps.get(key));
            }
        }
        return query;
    }

    /**
     * 通过sql查询
     *
     * @param sql
     * @param map
     * @return
     */
    @Override
    public List<Map<String, Object>> getDataByYourSql(String sql, Map<String, Object> map) {
        Query query= getQueryWithParameters(entityManager.createNativeQuery(sql),map);
        query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        return query.getResultList();
    }

    /**
     * 资源关闭
     *
     * @param connection
     * @param statement
     * @param set
     */
    @Override
    public void close(Connection connection, PreparedStatement statement, ResultSet set) {
        try {
            if (set != null) {
                set.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 批量插入
     *
     * @param list 实体类
     * @return Integer
     */
    @Override
    public Integer batchInsert(List<?> list) {
        if (!ObjectUtils.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                entityManager.persist(list.get(i));
                if (i % 50 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
            entityManager.flush();
            entityManager.clear();
        }
        return list.size();
    }

    /**
     * 批量更新
     *
     * @param list 实体类
     * @return Integer
     */
    @Override
    public Integer batchUpdate(List<?> list) {
        if (!ObjectUtils.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                entityManager.merge(list.get(i));
                if (i % 50 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
            entityManager.flush();
            entityManager.clear();
        }
        return list.size();
    }
}
