# 添加管理员时处理角色

添加管理员时，必须为新管理员分配至少1种角色，否则，管理员没有角色，就无法对应到某些权限，则此新管理员的账号基本上没有意义！

所以，需要在Service层进行调整，原本只是往管理员表中添加数据，现在，在同一个“添加管理员”的业务中，补充向“管理员与角色的关联表”中也添加数据。

则首先需要实现Mapper层的“向管理员与角色的关联表中批量插入数据”的功能（同一个管理员可以有多种角色），此功能此前已实现。

然后，在`AdminAddNewDTO`中添加新的属性，表示在添加管理员的页面中应该勾选的若干个角色ID：

```java
/**
 * 若干个角色的ID
 */
private Long[] roleIds;
```

在`AdminServiceImpl`中，补充自动装配`AdminRoleMapper`对象：

```java
@Autowired
private AdminRoleMapper adminRoleMapper;
```

在调用`AdminServiceImpl`中的`addNew()`方法中，补充“批量插入管理员与角色的关联数据”：

```java
// 准备批量插入管理员与角色的关联数据
Long adminId = admin.getId();
Long[] roleIds = adminAddNewDTO.getRoleIds();
AdminRole[] adminRoleList = new AdminRole[roleIds.length];
for (int i = 0; i < roleIds.length; i++) {
    AdminRole adminRole = new AdminRole();
    adminRole.setAdminId(adminId);
    adminRole.setRoleId(roleIds[i]);
    adminRoleList[i] = adminRole;
}
adminRoleMapper.insertBatch(adminRoleList);
```

完成后，可通过`AdminServiceTests`中的测试来验证执行效果，测试时，务必要给测试数据设置`roleIds`的值，例如：

```java
@Test
void addNew() {
    AdminAddNewDTO admin = new AdminAddNewDTO();
    admin.setUsername("管理员009");
    admin.setPassword("123456");
    admin.setPhone("13900139009");
    admin.setEmail("13900139009@baidu.com");
    admin.setRoleIds(new Long[]{4L, 5L, 6L}); // 重要

    try {
        service.addNew(admin);
        log.debug("添加数据完成！");
    } catch (ServiceException e) {
        log.debug("添加数据失败！名称已经被占用！");
    }
}
```

# 基于Spring JDBC的事务管理

事务（Transaction）：是数据库中的能够保证若干个**写操作**（增、删、改）要么全部成功，要么全部失败的机制。

> 提示：关于Spring JDBC框架，只要项目中添加了基于Spring的数据库编程的依赖项，都包含Spring JDBC框架的依赖项，例如在`mybatis-spring-boot-starter`中就包含了`spring-jdbc`依赖项。

在基于Spring JDBC的项目中，只需要在业务方法上添加`@Transactional`即可使得此业务方法是**事务性**的，即具有“要么全部成功，要么全部失败”的特性。

这是一种声明式声务（因为注解也是声明的一部分，只需要声明，即可实现事务管理）。

关于添加`@Transactional`注解：

- 【推荐--学习阶段】添加在接口上
  - 将作用于实现类中所有重写的方法
- 【推荐--开发实践中】添加在接口的业务方法上
  - 将作用于实现类中重写的此方法
- 添加在业务实现类上
  - 将作用于当前类中的所有业务方法
- 添加在业务实现类的业务方法上
  - 将作用于当前业务方法

**注意：**基于Spring JDBC的事务管理，是基于接口实现的，所以，必须存在接口，且`@Transactional`注解只对接口中声明的方法是有效的，对于实现类中自定义的方法是无效的！

事务管理的核心概念：

- **开启：**BEGIN
- **提交：**COMMIT
- **回滚：**ROLLBACK

在进行事务管理时，需要先**开启**事务，当所有的操作都正确的执行后，应该**提交**事务，此时，数据才会被写入到数据库存储中，如果执行过程中失败，应该**回滚**事务，则此前执行写操作都不会被写入到数据库存储中。

事务管理本质大致如下：

```
开启事务（关闭自动提交）
	-- 全部执行成功时：提交事务，即将数据写入到硬盘
	-- 出现错误时：回滚事务，放弃将已经成功执行的写操作写入到硬盘
打开自动提交
```

在Spring JDBC的事务管理中，大致如下：

```
try {
	【开启事务】
	执行你的业务方法
	【提交事务】
} catch (RuntimeException e) {
	【回滚事务】
}
```

**注意：**基于Spring JDBC的事务管理中，只对`RuntimeException`及其子孙类异常回滚！

在使用`@Transactional`注解时，可以配置`rollbackFor` / `rollbackForClassName`属性来指定对某些种类的异常回滚（仍必须是`RuntimeException`或其子孙类异常），也可以配置`noRollbackFor` / `noRollbackForClassName`属性来指定对某些种类的异常不回滚。

【关于Spring JDBC事务管理的小结】

- 当某个业务涉及超过1次增、删、改操作（例如2次增、1次增和1次改、1次删和1改，甚至更多次），必须保证此业务是事务性的
- 在接口中的抽象方法上添加`@Transactional`注解，即可使得此方法是事务性的
  - 在学习阶段，推荐在业务接口上添加此注解，避免遗漏
- 在实现业务的过程中，当视为“失败”时，应该抛出`RuntimeException`或其子孙类异常，使得事务管理机制执行回滚

基于以上原则，关于“添加管理员”，应该在`ServiceCode`中补充新的业务状态码：

```java
/**
 * 错误：插入数据错误
 */
ERR_INSERT(50000),
```

并在，在处理业务时，当执行插入数据操作时，应该及时获取“受影响的行数”，并判断此值是否符合预期值，当不受影响的行数不符合预期值时，应该抛出异常，例如：

```java
int rows = adminMapper.insert(admin);
if (rows != 1) {
    String message = "添加管理员失败，服务器忙，请稍后再尝试！";
    log.warn(message);
    throw new ServiceException(ServiceCode.ERR_INSERT, message);
}
```

```java
rows = adminRoleMapper.insertBatch(adminRoleList);
if (rows != roleIds.length) {
    String message = "添加管理员失败，服务器忙，请稍后再尝试！";
    log.warn(message);
    throw new ServiceException(ServiceCode.ERR_INSERT, message);
}
```

# 调整“删除管理员”的业务

由于添加管理员时向`ams_admin`和`ams_admin_role`这2张表中都插入了数据，那么，当删除管理员时，也应该同时删除这2张表中的相关数据！

**提示：**关于“根据管理员id删除关联表中的数据”的Mapper层功能，此前已经完成。

则调整`AdminServiceImpl`中`delete()`方法的实现（需要提前在`ServiceCode`中添加对应的业务状态码的枚举）：

```java
// 执行删除--管理员表
log.debug("即将执行删除数据，参数：{}", id);
int rows = adminMapper.deleteById(id);
if (rows != 1) {
    String message = "删除管理员失败，服务器忙，请稍后再尝试！";
    log.warn(message);
    throw new ServiceException(ServiceCode.ERR_DELETE, message);
}

// 执行删除--管理员与角色的关联表
rows = adminRoleMapper.deleteByAdminId(id);
if (rows < 1) {
    String message = "删除管理员失败，服务器忙，请稍后再尝试！";
    log.warn(message);
    throw new ServiceException(ServiceCode.ERR_DELETE, message);
}
```

# 关于Session的弊端

使用Session保存用户状态，存在几个问题：

- Session必须设置一个较短的有效期（通常不会超过半小时），超时将删除对应的Session数据，以缓存内存的压力
  - 此问题对于Session机制几乎无解
- Session是保存在服务器端内存中的数据，默认不支持集群项目
  - 此问题可以通过技术解决，例如使用共享Session

# 关于Token

Token：票据；令牌。也就是身份的凭证。

Token机制的典型表现：当用户成功登录后，服务器端会生成并响应一个Token到客户端，此Token上记录了用户的身份信息，此后，客户端在每次请求时都携带Token到服务器端，服务器端会先验证Token的真伪，当Token有效时，就可以根据Token上记录的信息来识别用户的身份。

Token是典型的解决集群系统甚至分布式系统中识别用户身份的解决方案。

由于Token本身并不占用服务器端的内存空间，所以，可以长时间的表示用户的身份，例如10天、15天甚至更久，这是Session机制无法解决的问题。

对于服务器端而言，主要解决几个问题：生成Token、验证Token真伪、解析Token。

# 关于JWT

**JWT**：**J**son **W**eb **T**oken

Token上可能需要记录用户身份的多项数据，例如`id`、`username`等，这些数据应该被有效的组织起来，以至于后续服务器端能够验证真伪，并解析出其中的数据，使用JWT时，这些数据是使用JSON格式组织起来的。

关于JWT的使用，有一套固定的标准，它约定了JWT数据的组成部分，必须包含：

- 头部信息（Header）
- 载荷（Payload）：数据
- 数据签名（Signature）

具体可参见：https://jwt.io/

# 使用JWT

首先，需要添加相关的依赖项，推荐的依赖项有：

```xml
<!-- JJWT（Java JWT） -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

生成JWT和解析JWT的示例（测试）代码如下：

```java
package cn.tedu.csmall.passport;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTests {
    
    // 是一个自定义的字符串，应该是一个保密数据，最低要求不少于4个字符，但推荐使用更加复杂的字符串
    String secretKey = "fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou";

    @Test
    void generate() {
        // JWT的过期时间
        Date date = new Date(System.currentTimeMillis() + 5 * 60 * 1000);

        // 你要存入到JWT中的数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", 9527);
        claims.put("username", "test-jwt");
        claims.put("phone", "13800138001");

        String jwt = Jwts.builder() // 获取JwtBuilder，准备构建JWT数据
                // 【1】Header：主要配置alg（algorithm：算法）和typ（type：类型）属性
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                // 【2】Payload：主要配置Claims，把你要存入的数据放进去
                .setClaims(claims)
                // 【3】Signature：主要配置JWT的过期时间、签名的算法和secretKey
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                // 完成
                .compact(); // 得到JWT数据
        System.out.println(jwt);
    }

    @Test
    void parse() {
        // 需要被解析的JWT，在复制此数据时，切记不要多复制了换行符（\n）
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwaG9uZSI6IjEzODAwMTM4MDAxIiwiaWQiOjk1MjcsImV4cCI6MTY3MTA5NTI3MiwidXNlcm5hbWUiOiJ0ZXN0LWp3dCJ9.9aHPOE-JLjCqd9sKEehoZzqGhz7hpsYcUwIzpiVdfmg";

        // 执行解析
        Claims claims = Jwts.parser() // 获得JWT解析工具
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();

        // 从Claims中获取生成时存入的数据
        Object id = claims.get("id");
        Object username = claims.get("username");
        Object phone = claims.get("phone");
        System.out.println("id = " + id);
        System.out.println("username = " + username);
        System.out.println("phone = " + phone);
    }

}
```

当尝试解析JWT时，如果JWT已经过期，则会出现`io.jsonwebtoken.ExpiredJwtException`：

```
io.jsonwebtoken.ExpiredJwtException: JWT expired at 2022-12-15T17:07:52Z. Current time: 2022-12-15T17:25:22Z, a difference of 1050481 milliseconds.  Allowed clock skew: 0 milliseconds.
```

当尝试解析JWT时，如果验证签名失败，则会出现`io.jsonwebtoken.SignatureException`：

```
io.jsonwebtoken.SignatureException: JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.
```

当尝试解析JWT时，如果因为JWT数据有误导致解析失败，则会出现`io.jsonwebtoken.MalformedJwtException`：

```
io.jsonwebtoken.MalformedJwtException: Unable to read JSON value: {"phone":"13800138001","id":9527,"exp":16%rname":"test-jwt"}
```

**注意：**不要在JWT中存入敏感信息，或核心数据，在不知道`secretKey`的情况下，依然可以根据JWT解析出相关数据，只是签名验证失败而已！例如，你可以将生成好的JWT粘贴到JWT官网上，可以看到JWT将解析出`Claims`中的信息，但是，会提示验证签名失败！所以，对于验证签名失败的JWT应该视为“错误的”，或“不可信任的”。

# 登录成功后响应JWT

首先，需要修改`IAdminService`中`login()`方法的返回值类型，由`void`改为`String`，表示认证通过后将返回JWT数据：

```java
/**
 * 管理员登录
 *
 * @param adminLoginDTO 封装了登录参数的对象
 * @return 管理员登录成功后将得到的JWT
 */
String login(AdminLoginDTO adminLoginDTO);
```

并且，修改`AdminServiceImpl`中重写的方法：

```java
@Override
public String login(AdminLoginDTO adminLoginDTO) {
    log.debug("开始处理【管理员登录】的业务，参数：{}", adminLoginDTO);
    // 执行认证
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            adminLoginDTO.getUsername(), adminLoginDTO.getPassword());
    Authentication authenticateResult
            = authenticationManager.authenticate(authentication);
    log.debug("认证通过！");
    log.debug("认证结果：{}", authenticateResult); // 注意：此认证结果中的Principal就是UserDetailsServiceImpl中返回的UserDetails对象

    // 从认证结果中取出将要存入到JWT中的数据
    Object principal = authenticateResult.getPrincipal();
    AdminDetails adminDetails = (AdminDetails) principal;
    Long id = adminDetails.getId();
    String username = adminDetails.getUsername();

    // 将认证通过后得到的认证信息存入到SecurityContext中
    // 【注意】注释以下2行代码后，在未完成JWT验证流程之前，用户的登录将不可用
    // SecurityContext securityContext = SecurityContextHolder.getContext();
    // securityContext.setAuthentication(authenticateResult);

    // ===== 生成并返回JWT =====
    // 是一个自定义的字符串，应该是一个保密数据，最低要求不少于4个字符，但推荐使用更加复杂的字符串
    String secretKey = "fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou";
    // JWT的过期时间
    Date date = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
    // 你要存入到JWT中的数据
    Map<String, Object> claims = new HashMap<>();
    claims.put("id", id);
    claims.put("username", username);
    // claims.put("权限", "???"); // TODO 待处理
    String jwt = Jwts.builder() // 获取JwtBuilder，准备构建JWT数据
            // 【1】Header：主要配置alg（algorithm：算法）和typ（type：类型）属性
            .setHeaderParam("alg", "HS256")
            .setHeaderParam("typ", "JWT")
            // 【2】Payload：主要配置Claims，把你要存入的数据放进去
            .setClaims(claims)
            // 【3】Signature：主要配置JWT的过期时间、签名的算法和secretKey
            .setExpiration(date)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            // 完成
            .compact(); // 得到JWT数据
    log.debug("即将返回JWT数据：{}", jwt);
    return jwt;
}
```

完成后，可以在`AdminServiceTests`中测试，当登录成功后，在日志中可以看到JWT数据：

```java
@Test
void login() {
    AdminLoginDTO adminLoginDTO = new AdminLoginDTO();
    adminLoginDTO.setUsername("liucangsong");
    adminLoginDTO.setPassword("123456");

    try {
        String jwt = service.login(adminLoginDTO);
        log.debug("登录成功，JWT：{}", jwt);
    } catch (Throwable e) {
        // 由于不确定Spring Security会抛出什么类型的异常
        // 所以，捕获的是Throwable
        // 并且，在处理时，应该打印信息，以了解什么情况下会出现哪种异常
        e.printStackTrace();
    }
}
```

以上测试时在控制台中输出的JWT，可以在`JwtTests`测试类中成功解析（注意：使用相同的`secretKey`）。

完成后，调整`AdminController`中处理登录的请求，当登录成功后，向客户端响应JWT数据：

```java
// http://localhost:9081/admins/login
@ApiOperation("管理员登录")
@ApiOperationSupport(order = 50)
@PostMapping("/login")
public JsonResult login(AdminLoginDTO adminLoginDTO) {
    log.debug("开始处理【管理员登录】的请求，参数：{}", adminLoginDTO);
    
    // ↓↓↓↓↓↓↓ 获取调用方法的返回结果，即JWT数据
    String jwt = adminService.login(adminLoginDTO);
    
    //                   ↓↓↓ 将JWT封装到响应对象中
    return JsonResult.ok(jwt);
}
```

完成后，重启项目，通过Knife4j的API文档进行调试，当登录成功后，将响应JWT数据。





















