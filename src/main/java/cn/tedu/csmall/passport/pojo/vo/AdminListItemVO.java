package cn.tedu.csmall.passport.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员的列表项VO类
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Data
public class AdminListItemVO implements Serializable {

    /**
     * 数据id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 简介
     */
    private String description;

    /**
     * 是否启用，1=启用，0=未启用
     */
    private Integer enable;

    /**
     * 最后登录IP地址（冗余）
     */
    private String lastLoginIp;

    /**
     * 累计登录次数（冗余）
     */
    private Integer loginCount;

    /**
     * 最后登录时间（冗余）
     */
    private LocalDateTime gmtLastLogin;

}