package cn.tedu.csmall.passport.security;

import cn.tedu.csmall.passport.mapper.AdminMapper;
import cn.tedu.csmall.passport.pojo.vo.AdminLoginInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        // 创建权限列表
        // AdminDetails的构造方法要求是Collection<? extends GrantedAuthority>类型的
        // 在Mapper查询结果中的权限是List<String>类型的，所以需要遍历再创建得到所需的权限列表
        List<String> permissions = loginInfo.getPermissions();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String permission : permissions) {
            GrantedAuthority authority = new SimpleGrantedAuthority(permission);
            authorities.add(authority);
        }

        // 创建AdminDetails类型的对象
        // 此类型是基于User类型扩展的，可以有自定义属性，例如id
        AdminDetails adminDetails = new AdminDetails(
                loginInfo.getId(), loginInfo.getUsername(), loginInfo.getPassword(),
                loginInfo.getEnable() == 1, authorities);

        log.debug("即将向Spring Security返回UserDetails对象：{}", adminDetails);
        return adminDetails;
    }

}
