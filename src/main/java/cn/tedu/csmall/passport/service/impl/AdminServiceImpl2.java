package cn.tedu.csmall.passport.service.impl;

import cn.tedu.csmall.passport.pojo.dto.AdminAddNewDTO;
import cn.tedu.csmall.passport.pojo.vo.AdminListItemVO;
import cn.tedu.csmall.passport.service.IAdminService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// @Service
public class AdminServiceImpl2 implements IAdminService {

    @Override
    public void addNew(AdminAddNewDTO adminAddNewDTO) {

    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public void setEnable(Long id) {

    }

    @Override
    public void setDisable(Long id) {

    }

    @Override
    public List<AdminListItemVO> list() {
        List<AdminListItemVO> list = new ArrayList<>();
        AdminListItemVO admin = new AdminListItemVO();
        admin.setUsername("Hahahahaha");
        list.add(admin);
        return list;
    }
}
