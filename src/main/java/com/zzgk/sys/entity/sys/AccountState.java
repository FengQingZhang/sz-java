package com.zzgk.sys.entity.sys;

import lombok.Data;

import javax.persistence.*;

/**
 * 账号状态表
 */
@Data
@Entity
@Table(name = "account_state")
public class AccountState {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    @Column(name="userid")
    private String userid;
    @Column(name="account_non_expired")
    private int accountNonExpired;
    @Column(name="account_non_locked")
    private int accountNonLocked;
    @Column(name="credentials_non_expired")
    private int credentialsNonExpired;
    @Column(name="enabled")
    private int enabled;
}
