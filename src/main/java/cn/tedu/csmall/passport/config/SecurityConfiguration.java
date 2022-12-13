package cn.tedu.csmall.passport.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Spring Security的配置类
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用“防止伪造的跨域攻击”这种防御机制
        http.csrf().disable();
    }

}
