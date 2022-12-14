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















