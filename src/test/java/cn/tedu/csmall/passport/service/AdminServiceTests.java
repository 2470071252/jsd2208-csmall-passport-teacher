package cn.tedu.csmall.passport.service;

import cn.tedu.csmall.passport.ex.ServiceException;
import cn.tedu.csmall.passport.pojo.dto.AdminAddNewDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class AdminServiceTests {

    @Autowired
    IAdminService service;

    @Test
    void addNew() {
        AdminAddNewDTO admin = new AdminAddNewDTO();
        admin.setUsername("管理员001");
        admin.setPassword("测试数据的简介");
        admin.setPhone("13900139001");
        admin.setEmail("13900139001@baidu.com");

        try {
            service.addNew(admin);
            log.debug("添加数据完成！");
        } catch (ServiceException e) {
            log.debug("添加数据失败！名称已经被占用！");
        }
    }

    @Test
    void setEnable() {
        Long id = 1L;

        try {
            service.setEnable(id);
            log.debug("启用管理员完成！");
        } catch (ServiceException e) {
            log.debug("启用管理员失败！具体原因请参见日志！");
        }
    }

    @Test
    void setDisable() {
        Long id = 1L;

        try {
            service.setDisable(id);
            log.debug("禁用管理员完成！");
        } catch (ServiceException e) {
            log.debug("禁用管理员失败！具体原因请参见日志！");
        }
    }

    @Test
    void list() {
        List<?> list = service.list();
        log.debug("查询列表完成，列表中的数据的数量：{}", list.size());
        for (Object item : list) {
            log.debug("{}", item);
        }
    }

}
