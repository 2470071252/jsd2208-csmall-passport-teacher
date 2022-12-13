# Spring Security框架

## Spring Security的作用

Spring Security主要解决了**认证**和**授权**相关的问题。

认证（Authenticate）：验证用户身份，即登录。

授权（Authorize）：允许用户访问受保护的资源，即某些请求需要特定的权限，检查用户是否有权限提交这些请求。

## Spring Security的依赖项

在Spring Boot项目中，当需要添加Spring Security的依赖时，依赖项为`spring-boot-starter-security`，即：

```xml
<!-- Spring Boot框架支持Security开发的依赖项，用于实现认证与授权 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**提示：**所有以`spring-boot-starter`为前缀的依赖项基本都有自动配置机制。

当添加以上依赖后，你的项目会发生以下变化：

- 所有请求都变成了**必须登录**的，无论请求路径是否存在，未登录时，都会重定向到`/login`的地址，显示Security提供的登录表单

- Spring Security提供了默认的用户名和密码，用户名为`user`，密码是启用项目时随机生成一个UUID值，在启动日志中可以看到：

  ![image-20221213091921614](images/image-20221213091921614.png)

- 当登录成功后，会重定向到此前尝试访问的页面，**注意：**由于此前尝试访问的页面可能本身就是不存在的，所以登录成功后可能会导致404错误

- 当登录成功后，所有`GET`请求都是允许正常访问的，但是，通过Knife4j的API文档的调试功能测试访问可以发现：所有的`POST`请求都是不允许访问的，访问时会响应`403`错误

- 当登录成功后，可以在浏览器中手动输入URL访问`/logout`路径，此页面是用于退出登录的：

  ![image-20221213092547230](images/image-20221213092547230.png)

  当成功的退出登录后，会重定向到登录页面，此时，回到所有请求都需要登录的状态

## 防止伪造的跨域攻击

默认情况下，即使登录成功，在API文档的调试功能中，所有`POST`请求都不能正常访问，这是因为Spring Security框架默认开启了“防止伪造的跨域攻击”这种防御机制。

伪造的跨域攻击，主要源自服务器端对客户端浏览器的信任，目前，主流的浏览器都是多选项卡的，只要在其中1个选项卡的页面中登录了，在同一个浏览器的其它任何选项卡的页面，都会是已经登录的状态，即使使用的不是多选项卡的浏览器，服务器端信任的也是整个浏览器，这是因为默认的认证机制是基于Session的，浏览器在对同一个服务器端提交请求时会自动携带同样的Session ID，所以，只要登录过，后续再携带同样的Session ID，无论是在哪个选项卡中，都会被视为“已登录”的状态！

基于这样的特点，假设用户在A选项卡中成功的登录某个银行的系统，而B选项卡打开是另一个网站，此网站中隐藏一个向银行发送转账的链接且是自动发出的，由于这2个选项卡是同一个浏览器打开的，所以，B选项卡中的页面发出的请求到了银行系统，银行系统也会视为“已登录”的状态，将执行转账操作。

PS：实际的转账还会有更多检查，例如再次输出密码、要求输出手机接收的验证码，不会如以上例子中直接转账。

Spring Security的防御机制表现为：所有POST请求必须提交某个值，这个值是由客户端向服务器端第一次发送请求时，由服务器端随机生成的，客户端会收到这个值，在后续的访问中，客户端必须提交此值，如果未提交，就会视为“伪造的跨域攻击”，将禁止访问！

例如，在Spring Security默认的登录页中：

![image-20221213102924586](images/image-20221213102924586.png)

由于我们开发的项目是前后端分离的，不可能得到以上这个随机值，所以，发出的`POST`请求全部被视为“伪造的跨域攻击”，所以导致了`403`错误！

在当前项目中，后续会实现基于JWT的认证机制，这种机制本身就是不会出现“伪造的跨域攻击”相关问题的，所以，直接将此防御机制禁用即可！

在项目的根包下创建`config.ScurityConfiguration`配置类，继承自`WebSecurityConfigurerAdapter`类，重写`void configure(HttpSecurity)`方法，在此方法中：

- 不调用父类的方法，即删除通过`super`调用父类方法的语句
  - 删除后，默认情况下，所有的请求都不需要登录了
- 添加`http.csrf().disable();`

## 关于请求是否需要认证

当项目添加了Spring Security的依赖后，所有请求默认都是需要认证（需要成功登录）的，当添加以上配置类，并删除了`super`调用父类方法后，所有请求都不再要求认证了！

在项目中，应该将某些请求配置为需要认证的，还有一些请求是不需要认证的！例如，在线API文档的相关页面应该是不需要认证即可访问的，而管理员管理的相关请求（例如添加管理员、删除管理员等）是需要认证才允许访问的！

则在配置类中的`void configure(HttpSecurity)`方法中添加以下配置：

```java
// 白名单URL
// 注意：所有路径使用 / 作为第1个字符
// 可以使用 * 通配符，例如 /admins/* 可以匹配 /admins/add-new，但是，不能匹配多级，例如不能匹配到 /admins/9527/delete
// 可以使用 ** 通配符，例如 /admins/** 可以匹配若干级，例如可以匹配 /admins/add-new，也可以匹配到 /admins/9527/delete
String[] urls = {
        "/doc.html",
        "/**/*.css",
        "/**/*.js",
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
```

**注意：**以上`anyRequest()`其实表示的是“任何请求”或者“所有请求”，并非“其它任何请求”！以上配置的机制是**优先原则**，例如“白名单”中的路径被配置为`permitAll()`，接下来，`anyRequest()`表示的范围其实也包含“白名单”中的所有路径，但是，不会覆盖此前的配置！

## 关于默认的登录页面

Spring Security默认的登录页面也是在`void configure(HttpSecurity)`方法中配置的，默认情况下，父类配置中是开启了登录表单的，如果子类（自定义的配置类继承自`WebSecurityConfigurerAdapter`）中没有通过`super`调用父类的方法，则不会开启登录表单！

在没有开启登录表单的情况下，如果被视为“未认证”，将响应`403`错误。

如果需要开启登录表单，可以在配置方法中添加：

```java
http.formLogin(); // 开启登录表单
```

## 关于`void configure(HttpSecurity)`方法的配置语法

关于请求的安全配置都是在`void configure(HttpSecurity http)`方法中调用参数对象的方法配置的，对于配置不同的内容，可以分开来配置，即使用多条语句，每条语句都调用参数`http`的方法，例如：

```java
// 所有请求都必须是通过认证的
http.authorizeRequests().anyRequest().authenticated();

// 禁用“防止伪造的跨域攻击”这种防御机制
http.csrf().disable();

http.formLogin(); // 开启登录表单
```

这些配置的设计也支持链式语法：

```java
http.authorizeRequests()
        .anyRequest()
        .authenticated()
        .and() // 重点
        .csrf().disable()
        .formLogin();
```

简单来说，如果要使用链式语法，当“打点”后不能调用相关的配置方法，就调用`and()`方法，此方法会返回当前参数对象，即`HttpSecurity`对象，然后继续“打点”进行其它配置。

并且，以上不冲突各配置可以不区分先后顺序。

## 使用自定义的用户名与密码登录

Spring Security在处理认证时，会自动调用`UserDetailsService`接口对象中的`UserDetails loadUserByUsername(String username)`方法，此方法是**根据用户名获取用户详情**的，此方法返回的结果中应该至少包括用户的密码及其它与登录密切相关的信息，例如账号的状态（是否启用等）、账号的权限等。

在整个处理过程中，Spring Security会根据表单中提交的用户名来调用此方法，并获得用户详情，接下来，由Spring Security去判断用户详情中的信息，例如密码是否正确、账号状态是否正常等。

![image-20221213141209492](images/image-20221213141209492.png)

在项目的根包下创建`security.UserDetailsServiceImpl`类，实现`UserDetailsService`接口，添加`@Service`注解，并重写接口中的方法：

```java
package cn.tedu.csmall.passport.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        log.debug("Spring Security调用了loadUserByUsername()方法，参数：{}", s);

        // 假设可用的用户名/密码是 root/1234
        if ("root".equals(s)) {
            UserDetails userDetails = User.builder()
                    .username("root")
                    .password("1234")
                    .disabled(false) // 账号是否禁用
                    .accountLocked(false) // 账号是否已锁定
                    .accountExpired(false) // 账号是否过期
                    .credentialsExpired(false) // 凭证是否过期
                    .authorities("这是一个山寨的临时权限，也不知道有什么用") // 权限
                    .build();
            return userDetails;
        }

        // 如果用户名不存在，暂时返回null
        return null;
    }

}
```

> 提示：以上类必须添加`@Service`注解，由于也在组件扫描的包下，所以，Spring会自动创建此类的对象，后续，Spring Security可以自动从容器中找到此类的对象并使用。

完成后，再次启用项目，在控制台可以看到Spring Security不再生成随机的UUID密码了，所以，原本的`user`临时账号已经不再可用，必须使用以上类中配置的账号密码才可以登录！









```java
@Autowired(required = false)
UserDetailsService userDetailsService;
```











## 提前准备：写出“根据用户名查询用户的登录信息”的Mapper功能，方法名可以使用`getLoginInfoByUsername`，提示：查询结果使用专门的VO类，例如`AdminLoginInfoVO`





