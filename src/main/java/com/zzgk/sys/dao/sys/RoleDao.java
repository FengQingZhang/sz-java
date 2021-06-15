package com.zzgk.sys.dao.sys;

import com.zzgk.sys.entity.sys.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Repository
public interface RoleDao extends JpaRepository<Role,Integer> {

    @Query(nativeQuery = true,value = "select role_id  from role_user where user_id=:userid")
    List<Integer> getRoleIdsByUserId(int userid);

    @Query(nativeQuery = true,value = "select r.* from role_user ru left join user u on ru.user_id = u.id left join " +
            "role r on ru.role_id = r.id where u.id=:userid ")
    List<Map<String,Object>> getRoleList(Integer userid);

    @Query(nativeQuery = true,value = "select r.* from  role_user ru left join user u on ru.user_id = u.id left join role r on ru.role_id = r.id where u.id=:userId ")
    List<Role> getRoleByUserId(int userId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = "update role set name= :#{#role.name}, " +
            "code= :#{#role.code}, " +
            "des= :#{#role.des} " +
            "where id = :#{#role.id}")
    void update(Role role);
}
