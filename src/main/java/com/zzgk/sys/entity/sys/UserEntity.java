package com.zzgk.sys.entity.sys;


import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 *  用户类
 */
@Data
@Entity
@Table(name = "user")
public class UserEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private Date create_date;
    private Date update_date;
}
