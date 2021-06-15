package com.zzgk.sys.entity.sys;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String token;
}
