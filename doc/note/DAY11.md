# 添加管理员--Service层（续）

**关于Service层的相关代码：**

- `ServiceCode`：从此前项目中复制，无改动

- `ServiceException`：从此前项目中复制，无改动

- `AdminAddNewDTO`：

  ```java
  @Data
  public class AdminAddNewDTO implements Serializable {
  
      /**
       * 用户名
       */
      private String username;
  
      /**
       * 密码（原文）
       */
      private String password;
  
      /**
       * 昵称
       */
      private String nickname;
  
      /**
       * 头像URL
       */
      private String avatar;
  
      /**
       * 手机号码
       */
      private String phone;
  
      /**
       * 电子邮箱
       */
      private String email;
  
      /**
       * 描述
       */
      private String description;
  
      /**
       * 是否启用，1=启用，0=未启用
       */
      private Integer enable;
  
  }
  ```

- `IAdminService`：

  ```java
  public interface IAdminService {
  
      void addNew(AdminAddNewDTO adminAddNewDTO);
  
  }
  ```

- `AdminServiceImpl`：

  ```java
  @Slf4j
  @Service
  public class AdminServiceImpl implements IAdminService {
  
      @Autowired
      private AdminMapper adminMapper;
  
      @Override
      public void addNew(AdminAddNewDTO adminAddNewDTO) {
          log.debug("开始处理【添加管理员】的业务，参数：{}", adminAddNewDTO);
          {
              // 从参数对象中取出用户名
              String username = adminAddNewDTO.getUsername();
              // 调用adminMapper.countByUsername()执行统计
              int count = adminMapper.countByUsername(username);
              // 判断统计结果是否大于0
              if (count > 0) {
                  // 是：抛出异常（ERR_CONFLICT）
                  String message = "添加管理员失败，尝试使用的用户名已经被占用！";
                  log.warn(message);
                  throw new ServiceException(ServiceCode.ERR_CONFLICT, message);
              }
          }
  
          {
              // 从参数对象中取出手机号码
              String phone = adminAddNewDTO.getPhone();
              // 调用adminMapper.countByPhone()执行统计
              int count = adminMapper.countByPhone(phone);
              // 判断统计结果是否大于0
              if (count > 0) {
                  // 是：抛出异常（ERR_CONFLICT）
                  String message = "添加管理员失败，尝试使用的手机号码已经被占用！";
                  log.warn(message);
                  throw new ServiceException(ServiceCode.ERR_CONFLICT, message);
              }
          }
  
          {
              // 从参数对象中取出电子邮箱
              String email = adminAddNewDTO.getEmail();
              // 调用adminMapper.countByEmail()执行统计
              int count = adminMapper.countByEmail(email);
              // 判断统计结果是否大于0
              if (count > 0) {
                  // 是：抛出异常（ERR_CONFLICT）
                  String message = "添加管理员失败，尝试使用的电子邮箱已经被占用！";
                  log.warn(message);
                  throw new ServiceException(ServiceCode.ERR_CONFLICT, message);
              }
          }
  
          // 创建Admin对象
          Admin admin = new Admin();
          // 复制参数DTO对象中的属性到实体对象中
          BeanUtils.copyProperties(adminAddNewDTO, admin);
          // TODO 将原密码加密，并修正属性值：admin.setPassword(xxx)
          // 补全属性值：admin.setLoginCount(0)
          admin.setLoginCount(0);
          // 调用adminMapper.insert()方法插入管理员数据
          adminMapper.insert(admin);
      }
  
  }
  ```

- `AdminServiceTests`：

  ```java
  @Slf4j
  @SpringBootTest
  public class AdminServiceTests {
  
      @Autowired
      IAdminService service;
  
      @Test
      void addNew() {
          AdminAddNewDTO admin = new AdminAddNewDTO();
          admin.setUsername("管理员001");
          admin.setPassword("测试数据的简介");
          admin.setPhone("13900139001");
          admin.setEmail("13900139001@baidu.com");
  
          try {
              service.addNew(admin);
              log.debug("添加数据完成！");
          } catch (ServiceException e) {
              log.debug("添加数据失败！名称已经被占用！");
          }
      }
  }
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

# 显示管理员列表--Mapper层

在项目的根包下创建`pojo.vo.AdminListItemVO`类：

```java
package cn.tedu.csmall.passport.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员的列表项VO类
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Data
public class AdminListItemVO implements Serializable {

    /**
     * 数据id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 简介
     */
    private String description;

    /**
     * 是否启用，1=启用，0=未启用
     */
    private Integer enable;

    /**
     * 最后登录IP地址（冗余）
     */
    private String lastLoginIp;

    /**
     * 累计登录次数（冗余）
     */
    private Integer loginCount;

    /**
     * 最后登录时间（冗余）
     */
    private LocalDateTime gmtLastLogin;

}
```

在`AdminMapper.java`接口中添加抽象方法，用于查询管理员列表：

```java
List<AdminListItemVO> list();
```

在`AdminMapper.xml`中配置：

```xml
<select id="list" resultMap="ListResultMap">
	SELECT
    	<include refid="ListQueryFields"/>
    FROM
    	ams_admin
    ORDER BY 
    	enable DESC, id
</select>

<sql id="ListQueryFields">
    <if test="true">
		id, username, …………
    </if>
</sql>

<resultMap id="ListResultMap" type="xx.xx.xx.AdminListItemVO">
    <id column="id" property="id"/>
    <result column="username" property="username"/>
    ... ...
</resultMap>
```

在`AlbumMapperTests`中编写并执行测试：

```java

```













