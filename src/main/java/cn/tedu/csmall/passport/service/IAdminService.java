package cn.tedu.csmall.passport.service;

import cn.tedu.csmall.passport.pojo.dto.AdminAddNewDTO;
import cn.tedu.csmall.passport.pojo.vo.AdminListItemVO;

import java.util.List;

public interface IAdminService {

    void addNew(AdminAddNewDTO adminAddNewDTO);

    void setEnable(Long id);
    void setDisable(Long id);

    /**
     * 查询管理员列表
     *
     * @return 管理员列表
     */
    List<AdminListItemVO> list();

}
