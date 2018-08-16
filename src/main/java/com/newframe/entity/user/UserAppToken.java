package com.newframe.entity.user;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * app用户的token
 *
 * This class corresponds to the database table user_app_token
 *
 * @mbggenerated do_not_delete_during_merge
 */
@Data
@Entity
@Table(name = "user_app_token")
public class UserAppToken {
    /**
     * 用户id
     * uid
     */
    @Id
    @Column(name = "uid")
    private Long uid;

    /**
     * app用户Token
     * token
     */
    @Column(name = "token")
    private String token;

    /**
     * 创建时间
     * ctime
     */
    @Column(name = "ctime")
    private Integer ctime;

    /**
     * 更新时间
     * utime
     */
    @Column(name = "utime")
    private Integer utime;
}