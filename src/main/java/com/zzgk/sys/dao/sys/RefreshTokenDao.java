package com.zzgk.sys.dao.sys;

import com.zzgk.sys.entity.sys.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface RefreshTokenDao extends JpaRepository<RefreshToken,Integer> {

    @Query(nativeQuery = true,value = "select token from refresh_token where username=:username")
    String getRefreshToken(String username);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = "update refresh_token set token= :#{#token.token} " +
            "where username = :#{#token.username}")
    void updateRefreshToken(RefreshToken token);

    @Query(nativeQuery = true,value = "select count(1) from refresh_token where username=:username")
    int existRefreshToken(String username);
}
