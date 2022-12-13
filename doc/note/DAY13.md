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

默认情况下，即使登录成功，在API文档的调试功能中，所有`POST`请求都不能正常访问！

在项目的根包下创建`config.ScurityConfiguration`配置类，继承自`WebSecurityConfigurerAdapter`类，重写`void configurer(HttpSecurity)`方法，在此方法中：

- 不调用父类的方法，即删除通过`super`调用父类方法的语句
  - 删除后，默认情况下，所有的请求都不需要登录了
- 添加`http.csrf().disable();`





