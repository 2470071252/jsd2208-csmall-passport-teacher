package cn.tedu.csmall.passport.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security的配置类
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // return NoOpPasswordEncoder.getInstance(); // NoOpPasswordEncoder是“不加密”的密码编码器
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // super.configure(http); // 父类方法配置要求所有请求都是认证过的

        // 白名单URL
        // 注意：所有路径使用 / 作为第1个字符
        // 可以使用 * 通配符，例如 /admins/* 可以匹配 /admins/add-new，但是，不能匹配多级，例如不能匹配到 /admins/9527/delete
        // 可以使用 ** 通配符，例如 /admins/** 可以匹配若干级，例如可以匹配 /admins/add-new，也可以匹配到 /admins/9527/delete
        String[] urls = {
                "/admins/login",
                "/doc.html",
                "/**/*.css",
                "/**/*.js",
                "/a.jpg",
                "/favicon.ico",
                "/swagger-resources",
                "/v2/api-docs"
        };

        // 配置请求是否需要认证
        http.authorizeRequests() // 配置请求的认证授权
                .mvcMatchers(urls) // 匹配某些请求路径
                .permitAll() // 允许直接访问，即不需要通过认证
                .anyRequest() // 其它任何请求
                .authenticated(); // 需要是通过认证的

        // 禁用“防止伪造的跨域攻击”这种防御机制
        http.csrf().disable();

        // http.formLogin(); // 开启登录表单
    }

}