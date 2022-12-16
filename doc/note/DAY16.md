# 在服务器端验证并解析JWT

**【重要】**当客户端成功通过认证（登录成功）后，客户端将得到服务器端响应的JWT，在后续的访问中，**客户端有义务携带JWT**来向服务器端发起请求，如果客户端未携带JWT，即使此前成功通过认证，服务器端也将视为“未通过认证（未登录）”。

**【重要】**服务器端应该尝试接收客户端携带的JWT数据，并尝试解析，并将解析得到的数据（例如管理员的`id`、用户名等）用于创建认证对象（`Authentication`），将此认证对象存入到`SecurityContext`中。

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













