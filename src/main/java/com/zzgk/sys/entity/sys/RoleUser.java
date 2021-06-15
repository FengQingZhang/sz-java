package com.zzgk.sys.entity.sys;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "role_user")
public class RoleUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int userid;
    private int role_id;
}
