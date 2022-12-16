# 在服务器端验证并解析JWT

**【重要】**当客户端成功通过认证（登录成功）后，客户端将得到服务器端响应的JWT，在后续的访问中，**客户端有义务携带JWT**来向服务器端发起请求，如果客户端未携带JWT，即使此前成功通过认证，服务器端也将视为“未通过认证（未登录）”。

**【重要】**服务器端应该尝试接收客户端携带的JWT数据，并尝试解析，并将解析得到的数据（例如管理员的`id`、用户名等）用于创建认证对象（`Authentication`），将此认证对象存入到`SecurityContext`中。

**【重要】**Spring Security框架始终根据`SecurityContext`中有没有认证信息来判断是否通过认证（是否已成功登录），也通过此认证信息来检查权限。

关于客户端携带JWT与服务器端接收JWT，业内惯用的做法是：服务器端会在请求头中名为`Authorization`的属性中获取JWT，则客户端应该按照此标准来提交请求。

在服务器端，应该在接收到任何请求的第一时间，就尝试获取JWT，则可以选用**过滤器（Filter）**来实现此效果。

在项目的根包下创建`filter.JwtAuthorizationFilter`类，继承自`OncePerRequestFilter`类，在类上添加`@Component`注解，并尝试获取JWT：

```java
@Slf4j
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
              HttpServletResponse response,
              FilterChain filterChain) throws ServletException, IOException {
        // 尝试从请求头中获取JWT
        String jwt = request.getHeader("Authorization");
        log.debug("尝试从请求头中获取JWT，结果：{}", jwt);
        
        // 放行请求，由后续的组件继续处理
        filterChain.doFilter(request, response);
    }

}
```

为了保证此过滤器能正常参与到Spring Security的处理流程中，需要在`SecurityConfiguration`中自动装配此过滤器的对象：

```java
@Autowired
private JwtAuthorizationFilter jwtAuthorizationFilter;
```

并在`void configure(HttpSecurity http)`方法中，将其添加：

```java
// 将JWT过滤器添加在Spring Security的“用户名密码认证信息过滤器”之前
http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

```

在Knife4j的API文档调试功能中，可以携带自定义的请求头数据：

![image-20221216093008751](images/image-20221216093008751.png)

![image-20221216093143812](images/image-20221216093143812.png)

完成后，重启服务器端项目，在API文档的调试中，发起任何请求，都可以在服务器端控制台看到接收到了JWT数据。

当接收到JWT后，应该对JWT数据进行最基本的检查，然后再尝试解析，例如，当接收到的JWT为`null`时，肯定没有必要尝试解析，同理，如果JWT为`""`空字符串，或仅有空白（空格、TAB制表位等）组成的字符串，都可以视为是无效的，可以通过`StringUtils.hasText()`方法进行检查。

另外，一个有效的JWT应该是`Header.Payload.Signature`这3部分组成的，各部分使用小数点分隔，其中，`Header`部分固定为36字符，`Signature`部分固定为43字符，中间的`Payload`根据存入的数据长度来决定，整个JWT数据的总长度至少113字符，所以，还可以做进一步的检查，例如要求JWT的长度至少113，甚至使用正则表达式进行检查。

需要注意的是：如果客户端携带的JWT是无效的，应该执行“放行”操作，不要因为JWT基本格式无效就返回错误，执行“放行”后，还会有Spring Security的其它组件来处理此请求，例如“白名单”路径的请求可以正常访问，其它路径的请求在没有获取到认证信息时将返回`403`。

所以，在接收到JWT后，先进行基本格式的检查：

```java
// 检查是否获取到了有效的JWT
if (!StringUtils.hasText(jwt) || jwt.length() < JWT_MIN_LENGTH) {
    // 对于无效的JWT，放行请求，由后续的组件继续处理
    log.debug("获取到的JWT被视为无效，当前过滤器将放行……");
    filterChain.doFilter(request, response);
    return;
}
```

接下来，即可尝试解析JWT：

```java
// 尝试解析JWT
log.debug("获取到的JWT被视为有效，准备解析JWT……");
String secretKey = "fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou";
Claims claims = Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(jwt)
        .getBody();

// 从Claims中获取生成时存入的数据
Long id = claims.get("id", Long.class);
String username = claims.get("username", String.class);
log.debug("从JWT中解析得到id：{}", id);
log.debug("从JWT中解析得到username：{}", username);
```

接下来，就可以把解析得到的管理员信息用于创建认证对象（`Authentication`）并存入到`SecurityContext`中。

典型的`Authentication`实现类是`UsernamePasswordAuthenticationToken`，这个类型有2个构造方法：

```java
public UsernamePasswordAuthenticationToken(Object principal, Object credentials) {
	// 暂不关心方法体的代码
}

public UsernamePasswordAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
	// 暂不关心方法体的代码
}
```

可以看到，这类认证信息中包括：当事人（Principal）、凭证（Credentials）、权限（Authorities）。

本次使用的认证对象中应该包括：当事人、权限，则后续Spring Security可以为添加了`@AuthenticationPrincipal`的参数注入值（当事人），或执行`@PreAuthorize`配置的方法上的权限检查，同时，由于此认证对象不用于判断密码，所以，不需要包括凭证部分。

由于当事人是一个`Object`类型的属性，想要同时表示`id`和`username`，必须将这2个属性封装起来，则在`sercurity`包下创建`LoginPrincipal`类型：

```java
@Data
public class LoginPrincipal implements Serializable {

    /**
     * 数据id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

}
```

在`JwtAuthorizationFilter`中，将解析JWT得到的`id`和`username`封装起来，用于创建`Authentication`对象（由于权限暂未处理，暂时随便创建一个）：

```java
// 将解析JWT得到的管理员信息创建成为AdminPrincipal（当事人）对象
LoginPrincipal loginPrincipal = new LoginPrincipal();
loginPrincipal.setId(id);
loginPrincipal.setUsername(username);

// 准备管理员权限
// 【临时】
List<SimpleGrantedAuthority> authorities = new ArrayList<>();
authorities.add(new SimpleGrantedAuthority("/ams/admin/read"));

// 创建Authentication对象，将存入到SecurityContext中
// 此Authentication对象必须包含：当事人（Principal）、权限（Authorities），不必包含凭证（Credentials）
Authentication authentication = new UsernamePasswordAuthenticationToken(loginPrincipal, null, authorities);
```

最后，将认证信息存入到`SecurityContext`中，并放行：

```java
/ 将Authentication对象存入到SecurityContext中
log.debug("即将向SecurityContext中存入认证信息：{}", authentication);
SecurityContext securityContext = SecurityContextHolder.getContext();
securityContext.setAuthentication(authentication);

// 放行请求，由后续的组件继续处理
log.debug("JWT过滤器执行完毕，放行！");
filterChain.doFilter(request, response);
```

至此，已经完成此过滤器的基础代码，包括：接收JWT、解析JWT、将登录信息创建为认证对象、将认证对象存入到`SecurityContext`中，当此过滤器放行后，后续执行的Spring Security组件就可以通过`SecurityContext`中的认证信息判断出“已经通过认证”的状态，并能够判断权限。

所以，重启项目，在API文档的调试中，携带有效的JWT即可成功进行相关访问。

**需要注意：**由于`SecurityContext`本身也是基于Session的，所以，在调试时，只要曾经携带有效的JWT访问过，就会将认证信息存入到`SecurityContext`中，在接下来的一段时间内，只要Session没有超时，即使不携带JWT也可以成功访问！其实，目前已经不再需要使用Session了，而且，不携带有效的JWT的访问本身就可以视为“无效”，所以，在当前过滤器刚刚执行时，可以清除`SecurityContext`，则`SecurityContext`中将不再包含认证信息，就可以避免刚才这种情况（曾经存入过认证信息，Session未超时就可以不携带JWT），从而实现“每次请求都必须携带JWT才会有认证信息”的效果。

关于清除`SecurityContext`的代码（以下代码应该在过滤器中方法的最开始位置）：

```java
// 清空SecurityContext
// 则SecurityContext中不再包含Authentication对象，也就没了认证信息
// 避免前序请求携带JWT且解析成功后向SecurityContext中存入认证信息，后续未超时的请求都可以不携带JWT的“问题”
SecurityContextHolder.clearContext();
```

至此，`JwtAuthorizationFilter`的完整代码为：

```java
package cn.tedu.csmall.passport.filter;

import cn.tedu.csmall.passport.security.LoginPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
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

        // 尝试解析JWT
        log.debug("获取到的JWT被视为有效，准备解析JWT……");
        String secretKey = "fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou";
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();

        // 从Claims中获取生成时存入的数据
        Long id = claims.get("id", Long.class);
        String username = claims.get("username", String.class);
        log.debug("从JWT中解析得到id：{}", id);
        log.debug("从JWT中解析得到username：{}", username);

        // 将解析JWT得到的管理员信息创建成为AdminPrincipal（当事人）对象
        LoginPrincipal loginPrincipal = new LoginPrincipal();
        loginPrincipal.setId(id);
        loginPrincipal.setUsername(username);

        // 准备管理员权限
        // 【临时】
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("/ams/admin/read"));

        // 创建Authentication对象，将存入到SecurityContext中
        // 此Authentication对象必须包含：当事人（Principal）、权限（Authorities），不必包含凭证（Credentials）
        Authentication authentication
                = new UsernamePasswordAuthenticationToken(loginPrincipal, null, authorities);

        // 将Authentication对象存入到SecurityContext中
        log.debug("即将向SecurityContext中存入认证信息：{}", authentication);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        // 放行请求，由后续的组件继续处理
        log.debug("JWT过滤器执行完毕，放行！");
        filterChain.doFilter(request, response);
    }

}
```

此时仍有一些问题没有解决，例如：

- 没有使用正确的权限
  - 以上代码中为每个携带JWT的请求给了一个固定的临时权限，与当前管理员的实际权限不符
- 没有处理解析JWT可能出现的异常
- 部分代码需要更规范
  - 解析JWT时使用的`secretKey`是当前类的局部变量，在生成JWT时也有一个同样的局部变量，导致在2个类中都声明了完全相同的局部变量，是不合理的

# 关于处理权限

在`AdminServiceImpl`处理“管理员登录”的业务时，如果登录成功，应该将权限写入到JWT中，则客户端收到的JWT就包含了权限信息，后续客户端携带“包含权限信息”的JWT提交请求，则JWT过滤器可以解析得到权限，并存入到`SecurityContext`中去。

为了保证权限列表（`Collection<? extends GrantedAuthority>`）写入到JWT中后，解析时还可以还原成原本的类型，应该在写入JWT之前，将权限列表转换成JSON格式的字符串，后续，解析JWT时，将此JSON格式的字符串还原成`Collection<? extends GrantedAuthority>`类型。

可以使用`fastjson`实现对象与JSON的相互转换，则在`pom.xml`中添加依赖：

```xml
<!-- fastjson：实现对象与JSON的相互转换 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.75</version>
</dependency>
```

然后，在`AdminServiceImpl`处理“管理员登录”的业务中，当通过认证后：

```java
Collection<GrantedAuthority> authorities = adminDetails.getAuthorities();
String authoritiesJsonString = JSON.toJSONString(authorities);
log.debug("认证结果中的当事人authorities：{}", authorities);
log.debug("认证结果中的当事人authoritiesJsonString：{}", authoritiesJsonString);
```

并将JSON字符串写入到JWT中：

```java
// 你要存入到JWT中的数据
Map<String, Object> claims = new HashMap<>();
claims.put("id", id);
claims.put("username", username);
claims.put("authorities", authoritiesJsonString); // 新增
```

在`JwtAuthorizationFilter`中即可从JWT中解析得到权限列表的JSON字符串：

```java
String authoritiesJsonString = claims.get("authorities", String.class);
log.debug("从JWT中解析得到authoritiesJsonString：{}", authoritiesJsonString);
```

并将其转换成所需的集合类型：

```java
// 准备管理员权限
List<SimpleGrantedAuthority> authorities
        = JSON.parseArray(authoritiesJsonString, SimpleGrantedAuthority.class);
```

至此，各管理员登录后，在认证信息中都有各自对应的权限（仍是根据JWT识别管理员）。

# 处理解析JWT时可能出现的异常

解析JWT时可能出现异常，但是，并不能使用此前的全局异常处理器来处理这几种异常，因为解析JWT是发生在过滤器中，过滤器是最早接收到请求的组件，此时其它组件（包括控制器、全局异常处理器等）都还没有开始处理此请求，而全局异常处理器只能处理由控制器抛出的异常，所以，此处并不适用，只能使用`try...catch`进行处理。

在`ServiceCode`中添加新的业务状态码，以对应解析JWT时可能出现的异常，然后，使用`try...catch`包裹解析JWT的代码，以处理异常：

```java
// 设置响应的文档类型，用于处理异常时
response.setContentType("application/json;charset=utf-8");

// 尝试解析JWT
log.debug("获取到的JWT被视为有效，准备解析JWT……");
String secretKey = "fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou";
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
```





在`application-dev.yml`中添加自定义配置：

```yaml
# 当前项目中的自定义配置
csmall:
  # JWT相关配置
  jwt:
    # 生成和解析JWT时使用的secretKey，此属性的值不得少于4个字符，建议在30~60字符之间，应该是一个不容易被猜测的值
    secret-key: fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou
```

然后，在`JwtAuthorizationFilter`和`AdminServiceImpl`中，都不再使用原有的`secretKey`局部变量（删除原有的那条语句），改为在类中通过`@Value`注解为全局的`secretKey`属性（新声明的）注入值：

```java
@Value("${csmall.jwt.secret-key}")
private String secretKey;
```

**注意：**如果你在配置文件中使用了新的`secretKey`值，请重新配置API文档中的全局参数，否则，将导致原本已经配置的JWT无法正常解析。

另外，也可以把JWT的有效时长也配置在`application-dev.yml`中：

```yaml
csmall:
  jwt:
    secret-key: fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou
    # 【新增】JWT的有效时长，以分钟为单位
    duration-in-minute: 10080
```

并在`AdminServiceImpl`类中将此配置值读取到属性中：

```java
@Value("${csmall.jwt.duration-in-minute}")
private long durationInMinute;
```

然后，在设置JWT过期时间时，应用此配置值：

```java
// JWT的过期时间
Date date = new Date(System.currentTimeMillis() + durationInMinute * 60 * 1000);
```









