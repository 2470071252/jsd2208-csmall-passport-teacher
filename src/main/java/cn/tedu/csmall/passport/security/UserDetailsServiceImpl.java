package cn.tedu.csmall.passport.security;

import cn.tedu.csmall.passport.mapper.AdminMapper;
import cn.tedu.csmall.passport.pojo.vo.AdminLoginInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        log.debug("Spring Security调用了loadUserByUsername()方法，参数：{}", s);

        AdminLoginInfoVO loginInfo = adminMapper.getLoginInfoByUsername(s);
        log.debug("从数据库查询用户名【{}】匹配的信息，结果：{}", s, loginInfo);

        if (loginInfo == null) {
            return null; // 暂时
        }

        UserDetails userDetails = User.builder()
                .username(loginInfo.getUsername())
                .password(loginInfo.getPassword())
                .disabled(loginInfo.getEnable() == 0)
                .accountLocked(false) // 账号是否已锁定
                .accountExpired(false) // 账号是否过期
                .credentialsExpired(false) // 凭证是否过期
                .authorities("这是一个山寨的临时权限，也不知道有什么用") // 权限
                .build();
        log.debug("即将向Spring Security返回UserDetails对象：{}", userDetails);
        return userDetails;
    }

}
