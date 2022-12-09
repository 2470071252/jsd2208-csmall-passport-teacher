# 添加管理员--Service层（续）

**关于Service层的相关代码：**

- `ServiceCode`：从此前项目中复制，无改动

- `ServiceException`：从此前项目中复制，无改动

- `AdminAddNewDTO`：

  ```java
  
  ```

- `IAdminService`：

  ```java
  
  ```

- `AdminServiceImpl`：

  ```java
  
  ```

- `AdminServiceTests`：

  ```java
  
  ```

# 添加管理员--Controller层

从前序项目中复制`JsonResult`、`GlobalExceptionHandler`到当前项目对应的位置。

在项目的根包下创建`controller.AdminController`类，作为处理管理员相关请求的控制器类，在类上添加`@RestController`和`@RequestMapping("/admins")`，及其它辅助注解：`@Api(tags = "管理员管理模块")`、`@Slf4j`：

```java
@Slf4j
@RestController
@RequestMapping("/admins")
@Api(tags = "管理员管理模块")
public class AdminController {}
```

并在类中处理请求：

```java
@Autowired
private IAdminService adminService;

@ApiOperation("添加管理员")
@ApiOperationSupport(order = 100)
@PostMapping("/add-new")
public JsonResult addNew(AdminAddNewDTO adminAddNewDTO) {
    // 日志
    // 调用Service方法
    return JsonResult.ok();
}
```













