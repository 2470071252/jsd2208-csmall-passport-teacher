package cn.tedu.csmall.passport.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色的列表项VO类
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Data
public class RoleListItemVO implements Serializable {

    /**
     * 数据id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 自定义排序序号
     */
    private Integer sort;

}