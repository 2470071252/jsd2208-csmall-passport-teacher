package cn.tedu.csmall.passport.mapper;

import cn.tedu.csmall.passport.pojo.entity.AdminRole;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class AdminRoleMapperTests {

    @Autowired
    AdminRoleMapper mapper;

    @Test
    void insertBatch() {
        AdminRole[] adminRoles = new AdminRole[3];
        for (int i = 0; i < adminRoles.length; i++) {
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(100L);
            adminRole.setRoleId(i + 0L);
            adminRoles[i] = adminRole;
        }

        int rows = mapper.insertBatch(adminRoles);
        log.debug("批量插入完成，受影响的行数：{}", rows);
    }

    @Test
    void deleteByAdminId() {
        Long adminId = 6L;
        int rows = mapper.deleteByAdminId(adminId);
        log.debug("删除完成，受影响的行数：{}", rows);
    }

}
