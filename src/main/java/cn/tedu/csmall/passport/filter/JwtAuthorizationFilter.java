package cn.tedu.csmall.passport.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    public JwtAuthorizationFilter() {
        log.debug("创建过滤器对象：JwtAuthorizationFilter");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 尝试从请求头中获取JWT
        String jwt = request.getHeader("Authorization");
        log.debug("尝试从请求头中获取JWT，结果：{}", jwt);

        // 检查是否获取到了JWT
        if (!StringUtils.hasText(jwt) || jwt.length() < JWT_MIN_LENGTH) {
            // 对于无效的JWT，放行请求，由后续的组件继续处理
            log.debug("获取到的JWT被视为无效，当前过滤器将放行……");
            filterChain.doFilter(request, response);
            return;
        }

        // 尝试解析JWT
        log.debug("获取到的JWT被视为有效，准备解析JWT……");
        String secretKey = "fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou";
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();

        // 从Claims中获取生成时存入的数据
        Object id = claims.get("id");
        Object username = claims.get("username");
        log.debug("从JWT中解析得到id：{}", id);
        log.debug("从JWT中解析得到username：{}", username);

        // 放行请求，由后续的组件继续处理
        filterChain.doFilter(request, response);
    }

}
