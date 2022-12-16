package cn.tedu.csmall.passport.security;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminPrincipal implements Serializable {

    /**
     * 数据id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

}
