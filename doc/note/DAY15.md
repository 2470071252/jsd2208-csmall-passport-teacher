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

