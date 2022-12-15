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







