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