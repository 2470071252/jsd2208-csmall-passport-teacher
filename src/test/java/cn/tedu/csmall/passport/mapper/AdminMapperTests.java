package cn.tedu.csmall.passport.mapper;

import cn.tedu.csmall.passport.pojo.entity.Admin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class AdminMapperTests {

    @Autowired
    AdminMapper mapper;

    @Test
    void insert() {
        Admin admin = new Admin();
        admin.setUsername("wangkejing001");
        admin.setPassword("123456");
        admin.setPhone("13800138001");
        admin.setEmail("wangkejing001@baidu.com");

        log.debug("插入数据之前，参数：{}", admin);
        int rows = mapper.insert(admin);
        log.debug("插入数据完成，受影响的行数：{}", rows);
        log.debug("插入数据之后，参数：{}", admin);
    }

    @Test
    void countByUsername() {
        String username = "wangkejing";
        int count = mapper.countByUsername(username);
        log.debug("根据用户名【{}】统计管理员账号的数量：{}", username, count);
    }

    @Test
    void countByPhone() {
        String phone = "13800138001";
        int count = mapper.countByPhone(phone);
        log.debug("根据手机号码【{}】统计管理员账号的数量：{}", phone, count);
    }

    @Test
    void countByEmail() {
        String email = "wangkejing@baidu.com";
        int count = mapper.countByEmail(email);
        log.debug("根据电子邮箱【{}】统计管理员账号的数量：{}", email, count);
    }

}
