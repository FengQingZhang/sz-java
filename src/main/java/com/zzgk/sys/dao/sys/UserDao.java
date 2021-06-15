package com.zzgk.sys.dao.sys;

import com.zzgk.sys.entity.sys.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserDao extends JpaRepository<UserEntity,Integer> {

    @Query(nativeQuery = true, value = "select * from user where username=:username")
    UserEntity findByUserName(@Param("username")String username);

    /**
     * 根据用户id查询权限
     * @param userId
     * @return
     */
    @Query(nativeQuery = true,value = "select r.* from  sys_role_user ru left join user u on ru.user_id = u.id left join sys_role r on ru.role_id = r.id where u.id=:userId ")
    List<Map<String,Object>> getRoleByUserId(@Param("userId")int userId);
}
