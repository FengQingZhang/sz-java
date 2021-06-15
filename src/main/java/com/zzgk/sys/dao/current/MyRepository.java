package com.zzgk.sys.dao.current;

import com.zzgk.sys.entity.current.IPagination;

import javax.persistence.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public interface MyRepository{
    /**
     * 创建表格
     * @param sql sql语句
     */
    void createTable(String sql);

    /**
     * 更新数据
     * @param sql sql语句
     * @param maps 参数和value的map
     * @return int
     */
    int updateData(String sql, Map<String,Object> maps);

    /**
     * 表格是否存在
     * @param tableName 表格名称
     * @return boolean
     */
    boolean isTableExist(String tableName);

    /**
     * 分页搜索
     * @param tableName 表名
     * @param where where语句部分
     * @param map 参数名和value的map
     * @param page 页码
     * @param size 每页数据量
     * @return
     */
    IPagination<Map<String,Object>> searchReturnIPagination(String tableName,String where,Map<String,Object> map,
                                           int page,int size);

    /**
     * 返回IPagination<Map>
     * @param page 页码
     * @param size 每页数量
     * @param countSql 查数量sql
     * @param sql 查询sql
     * @param maps 参数和value的map
     * @return
     */
    IPagination<Map<String,Object>> search(int page,int size,String countSql,String sql,Map<String,Object> maps);


    /**
     * 创建参数映射
     * @param query query
     * @param maps 参数和值的map
     * @return Query
     */
    Query getQueryWithParameters(Query query, Map<String,Object> maps);

    /**
     * 通过sql查询
     * @param sql
     * @param map
     * @return
     */
    List<Map<String,Object>> getDataByYourSql(String sql,Map<String,Object> map);

    /**
     * 资源关闭
     */
    void close(Connection connection, PreparedStatement statement, ResultSet set);

    /**
     * 批量更新
     * @param list 实体类
     * @return Integer
     */
    Integer batchUpdate(List<?> list);

    /**
     * 批量插入
     * @param list 实体类
     * @return Integer
     */
    Integer batchInsert(List<?> list);

}
