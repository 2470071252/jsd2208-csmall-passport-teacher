package cn.tedu.csmall.passport.service;

import cn.tedu.csmall.passport.pojo.dto.AdminAddNewDTO;
import cn.tedu.csmall.passport.pojo.dto.AdminLoginDTO;
import cn.tedu.csmall.passport.pojo.vo.AdminListItemVO;

import java.util.List;

/**
 * 处理管理员数据的业务接口
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
public interface IAdminService {

    /**
     * 管理员登录
     * @param adminLoginDTO 封装了登录参数的对象
     */
    void login(AdminLoginDTO adminLoginDTO);

    /**
     * 添加管理员
     *
     * @param adminAddNewDTO 管理员数据
     */
    void addNew(AdminAddNewDTO adminAddNewDTO);

    /**
     * 删除管理员
     *
     * @param id 管理员id
     */
    void delete(Long id);

    /**
     * 启用管理员
     *
     * @param id 管理员id
     */
    void setEnable(Long id);

    /**
     * 禁用管理员
     *
     * @param id 管理员id
     */
    void setDisable(Long id);

    /**
     * 查询管理员列表
     *
     * @return 管理员列表
     */
    List<AdminListItemVO> list();

}
