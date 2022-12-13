package cn.tedu.csmall.passport.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员登录的DTO类
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Data
public class AdminLoginDTO implements Serializable {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（原文）
     */
    private String password;

}