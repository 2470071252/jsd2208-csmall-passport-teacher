package cn.tedu.csmall.passport.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        log.debug("Spring Security调用了loadUserByUsername()方法，参数：{}", s);

        // 假设可用的用户名/密码是 root/1234
        if ("root".equals(s)) {
            UserDetails userDetails = User.builder()
                    .username("root")
                    .password("1234")
                    .disabled(false) // 账号是否禁用
                    .accountLocked(false) // 账号是否已锁定
                    .accountExpired(false) // 账号是否过期
                    .credentialsExpired(false) // 凭证是否过期
                    .authorities("这是一个山寨的临时权限，也不知道有什么用") // 权限
                    .build();
            return userDetails;
        }

        // 如果用户名不存在，暂时返回null
        return null;
    }

}
