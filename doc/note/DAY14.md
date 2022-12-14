## 关于密码加密

由于目前已经配置了`PasswordEncoder`，具体类型是`BcryptPasswordEncoder`，则Spring Security在处理认证时，会自动使用它，就要求所有的被查询出来的密码都是密文，所以，在添加管理员时，密码也需要使用这种编码进行处理成密文再保存到数据库！

在`AdminServiceImpl`中，先自动装配`PasswordEncoder`对象：

```java
@Autowired
private PasswordEncoder passwordEncoder;
```

然后在`addNew()`方法中，在插入管理员数据之前：

```java
// 将原密码加密
String rawPassword = admin.getPassword();
String encodedPassword = passwordEncoder.encode(rawPassword);
admin.setPassword(encodedPassword);
```

完成后，可以通过`AdminServiceTests`测试检验效果（不要通过在线API文档去访问）。

## 关于处理异常

在处理登录时，是由Spring Security处理的，当登录失败时，会由Spring Security抛出异常，所以，可以统一处理，对于用户名错误、密码错误这种的处理方式可以是完全相同的，但，原本的异常类型并不相同，用户名不存在时抛出的是`InternalAuthenticationServiceException`，密码错误时抛出的是`BadCredentialsException`。

相关异常的继承结构如下：

```
AuthenticationException
  -- BadCredentialsException（密码错误时抛出的异常）
  -- AuthenticationServiceException
    -- InternalAuthenticationServiceException（用户名不存在时抛出的异常）
  -- AccountStatusException
    -- DisabledException（账号被禁用时抛出的异常）
```

为了准确的表达只处理`InternalAuthenticationServiceException`和`BadCredentialsException`这2种异常，可以配置`@ExceptionHandler`注解参数，例如：

```java
@ExceptionHandler({
        InternalAuthenticationServiceException.class,
        BadCredentialsException.class
})
public JsonResult handleAuthenticationException(AuthenticationException e) {
    log.debug("开始处理AuthenticationException");
    log.debug("异常类型：" + e.getClass().getName());
    log.debug("异常消息：" + e.getMessage());
    // log.debug("跟踪信息：");
    // e.printStackTrace();
    String message = "登录失败，用户名或密码错误！";
    return JsonResult.fail(ServiceCode.ERR_UNAUTHORIZED, message);
}
```

另外，建议在全局异常处理器中，添加对`Throwable`异常的处理，避免向客户端响应`500`错误，例如：

```java
@ExceptionHandler
public JsonResult handleThrowable(Throwable e) {
    log.debug("开始处理Throwable");
    e.printStackTrace();
    String message = "服务器忙，请稍后再尝试（开发阶段，请检查服务器端控制台）！";
    // 需要先在ServiceCode中补充新的业务状态码ERR_UNKNOWN，值应该使用比较特殊的
    return JsonResult.fail(ServiceCode.ERR_UNKNOWN, message); 
}
```

**注意：**以上代码中的`e.printStackTrace();`是耗时操作，可能导致线程阻塞，在许多项目中是禁止使用的，在项目上线之前，应该评估是否需要删除此行代码！

## 关于Spring Security判断是否通过认证的标准

在Spring Security框架中，有`SecurityContext`，它是用于持有各用户的认证信息的，即各用户成功登录后，需要将认证信息存入到`SecurityContext`中，后续，Spring Security框架会自动检查`SecurityContext`中的认证信息，如果某个用户在`SecurityContext`中没有匹配的认证信息，将被视为“未通过认证”（未登录）的状态。

在项目的任何代码片段中，都可以通过`SecurityContextHolder`类的静态方法`getContext()`来获取`SecuriytContext`的引用，例如：

```java
SecurityContext securityContext = SecurityContextHolder.getContext();
```

在处理认证的过程中，当视为“已认证”时，需要将认证信息存入到`SecurityContext`中！目前，是通过`AuthenticationManager`对象的`authenticate()`方法执行认证的，此方法认证通过后，会返回`Authentication`对象，即认证信息，将它存入到`SecurityContext`中即可！

则在`AdminServiceImpl`中的`login()`方法中进行调整：

```java
@Override
public void login(AdminLoginDTO adminLoginDTO) {
    log.debug("开始处理【管理员登录】的业务，参数：{}", adminLoginDTO);
    // 执行认证
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            adminLoginDTO.getUsername(), adminLoginDTO.getPassword());
    
    // ↓↓↓↓↓ 调整：获取方法的返回值
    Authentication authenticateResult
            = authenticationManager.authenticate(authentication);
    log.debug("认证通过！");
    
	// ↓↓↓↓↓ 新增以下2行代码
    // 将认证通过后得到的认证信息存入到SecurityContext中
    SecurityContext securityContext = SecurityContextHolder.getContext();
    securityContext.setAuthentication(authenticateResult);
}
```

## 关于管理员（用户）的权限设计

当前项目中，权限使用了 RBAC 的设计原则，具体可参考《CoolShark商城数据库与数据表设计(v1.0)-03.后台管理员管理相关数据表设计.pdf》。

## 实现访问控制

在`csmall_ams.sql`脚本插入中的测试数据中，已经给出了权限、角色、管理员及相关的关联测试数据，也就是说，各管理员账号都有关联的角色，各角色也有关联的权限！

当管理员尝试登录时，应该读取此管理员的权限，最终，存入到`SecurityContext`中，则Spring Security随时可以知道此管理员的权限，并判断是否允许执行某些操作，以实现访问控制！

首先，需要修改Mapper层原有的`getLoginInfoByUsername()`方法的查询，要求查出管理员的权限！需要执行的SQL语句大致是：

```mysql
SELECT
    ams_admin.id,
    ams_admin.username,
    ams_admin.password,
    ams_admin.enable,
    ams_permission.value
FROM ams_admin
LEFT JOIN ams_admin_role ON ams_admin.id=ams_admin_role.admin_id
LEFT JOIN ams_role_permission ON ams_admin_role.role_id=ams_role_permission.role_id
LEFT JOIN ams_permission ON ams_role_permission.permission_id=ams_permission.id
WHERE username='root';
```













