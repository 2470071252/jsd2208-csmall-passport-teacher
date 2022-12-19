package cn.tedu.csmall.passport.filter;

import cn.tedu.csmall.passport.security.LoginPrincipal;
import cn.tedu.csmall.passport.web.JsonResult;
import cn.tedu.csmall.passport.web.ServiceCode;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * <p>JWT过滤器</p>
 *
 * <p>此JWT的作用：</p>
 * <ul>
 *     <li>获取客户端携带的JWT，要求客户端将JWT存放在请求头的Authorization属性中</li>
 *     <li>解析客户端携带的JWT，并创建Authentication对象，存入到SecurityContext中</li>
 * </ul>
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Slf4j
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    public static final int JWT_MIN_LENGTH = 113;

    @Value("${csmall.jwt.secret-key}")
    private String secretKey;

    public JwtAuthorizationFilter() {
        log.debug("创建过滤器对象：JwtAuthorizationFilter");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 清空SecurityContext
        // 则SecurityContext中不再包含Authentication对象，也就没了认证信息
        // 避免前序请求携带JWT且解析成功后向SecurityContext中存入认证信息，后续未超时的请求都可以不携带JWT的“问题”
        SecurityContextHolder.clearContext();

        // 尝试从请求头中获取JWT
        String jwt = request.getHeader("Authorization");
        log.debug("尝试从请求头中获取JWT，结果：{}", jwt);

        // 检查是否获取到了有效的JWT
        if (!StringUtils.hasText(jwt) || jwt.length() < JWT_MIN_LENGTH) {
            // 对于无效的JWT，放行请求，由后续的组件继续处理
            log.debug("获取到的JWT被视为无效，当前过滤器将放行……");
            filterChain.doFilter(request, response);
            return;
        }

        // 设置响应的文档类型，用于处理异常时
        response.setContentType("application/json;charset=utf-8");

        // 尝试解析JWT
        log.debug("获取到的JWT被视为有效，准备解析JWT……");
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (ExpiredJwtException e) {
            String message = "您的登录信息已过期，请重新登录！";
            log.warn("解析JWT时出现ExpiredJwtException，响应的消息：{}", message);
            JsonResult jsonResult = JsonResult.fail(ServiceCode.ERR_JWT_EXPIRED, message);
            PrintWriter writer = response.getWriter();
            writer.println(JSON.toJSONString(jsonResult));
            writer.close();
            return;
        } catch (SignatureException e) {
            String message = "非法访问！";
            log.warn("解析JWT时出现SignatureException，响应的消息：{}", message);
            JsonResult jsonResult = JsonResult.fail(ServiceCode.ERR_JWT_SIGNATURE, message);
            PrintWriter writer = response.getWriter();
            writer.println(JSON.toJSONString(jsonResult));
            writer.close();
            return;
        } catch (MalformedJwtException e) {
            String message = "非法访问！";
            log.warn("解析JWT时出现MalformedJwtException，响应的消息：{}", message);
            JsonResult jsonResult = JsonResult.fail(ServiceCode.ERR_JWT_MALFORMED, message);
            PrintWriter writer = response.getWriter();
            writer.println(JSON.toJSONString(jsonResult));
            writer.close();
            return;
        } catch (Throwable e) {
            String message = "服务器忙，请稍后再尝试（开发阶段，请检查服务器端控制台）！";
            log.warn("解析JWT时出现{}，响应的消息：{}", e.getClass().getName(), message);
            e.printStackTrace();
            JsonResult jsonResult = JsonResult.fail(ServiceCode.ERR_UNKNOWN, message);
            PrintWriter writer = response.getWriter();
            writer.println(JSON.toJSONString(jsonResult));
            writer.close();
            return;
        }

        // 从Claims中获取生成时存入的数据
        Long id = claims.get("id", Long.class);
        String username = claims.get("username", String.class);
        String authoritiesJsonString = claims.get("authorities", String.class);
        log.debug("从JWT中解析得到id：{}", id);
        log.debug("从JWT中解析得到username：{}", username);
        log.debug("从JWT中解析得到authoritiesJsonString：{}", authoritiesJsonString);

        // 将解析JWT得到的管理员信息创建成为AdminPrincipal（当事人）对象
        LoginPrincipal loginPrincipal = new LoginPrincipal();
        loginPrincipal.setId(id);
        loginPrincipal.setUsername(username);

        // 准备管理员权限
        List<SimpleGrantedAuthority> authorities
                = JSON.parseArray(authoritiesJsonString, SimpleGrantedAuthority.class);

        // 创建Authentication对象，将存入到SecurityContext中
        // 此Authentication对象必须包含：当事人（Principal）、权限（Authorities），不必包含凭证（Credentials）
        Authentication authentication
                = new UsernamePasswordAuthenticationToken(
                loginPrincipal, null, authorities);

        // 将Authentication对象存入到SecurityContext中
        log.debug("即将向SecurityContext中存入认证信息：{}", authentication);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        // 放行请求，由后续的组件继续处理
        log.debug("JWT过滤器执行完毕，放行！");
        filterChain.doFilter(request, response);
    }

}