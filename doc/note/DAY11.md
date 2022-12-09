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
<!-- List<AdminListItemVO> list(); -->
<select id="list" resultMap="ListResultMap">
    SELECT
        <include refid="ListQueryFields"/>
    FROM
        ams_admin
    ORDER BY
        id
</select>

<sql id="ListQueryFields">
    <if test="true">
        id, username, nickname, avatar, phone,
        email, description, enable, last_login_ip, login_count,
        gmt_last_login
    </if>
</sql>

<resultMap id="ListResultMap" type="cn.tedu.csmall.passport.pojo.vo.AdminListItemVO">
    <id column="id" property="id"/>
    <result column="username" property="username"/>
    <result column="nickname" property="nickname"/>
    <result column="avatar" property="avatar"/>
    <result column="phone" property="phone"/>
    <result column="email" property="email"/>
    <result column="description" property="description"/>
    <result column="enable" property="enable"/>
    <result column="last_login_ip" property="lastLoginIp"/>
    <result column="login_count" property="loginCount"/>
    <result column="gmt_last_login" property="gmtLastLogin"/>
</resultMap>
```

在`AdminMapperTests`中编写并执行测试：

```java
@Test
void list() {
    List<?> list = mapper.list();
    log.debug("查询列表完成，列表中的数据的数量：{}", list.size());
    for (Object item : list) {
        log.debug("{}", item);
    }
}
```

# 显示管理员列表--Service层

在`IAdminService`中添加抽象方法：

```java
/**
 * 查询管理员列表
 *
 * @return 管理员列表
 */
List<AdminListItemVO> list();
```

在`AdminServiceImpl`中实现此方法：

```java
@Override
public List<AdminListItemVO> list() {
    log.debug("开始处理【查询管理员列表】的业务，参数：无");
    List<AdminListItemVO> list = adminMapper.list();
    return list;
}
```

在`AdminServiceTests`中测试：

```java
@Test
void list() {
    List<?> list = service.list();
    log.debug("查询列表完成，列表中的数据的数量：{}", list.size());
    for (Object item : list) {
        log.debug("{}", item);
    }
}
```

# 显示管理员列表--Controller层

在`AdminController.java`中添加处理请求的方法：

```java
// http://localhost:9081/admins
@ApiOperation("查询管理员列表")
@ApiOperationSupport(order = 420)
@GetMapping("")
public JsonResult list() {
    log.debug("开始处理【查询管理员列表】的请求，参数：无");
    List<AdminListItemVO> list = adminService.list();
    return JsonResult.ok(list);
}
```

完成后，重启项目，通过API文档的调试功能，可以查询到相册列表。

# 启用或禁用管理员--Mapper层

启用或禁用管理员的操作，本质上是修改管理员数据的`enable`属性的值，所以，是一种`UPDATE`操作。

在Mapper层，每张表应该只需要1个`UPDATE`操作来实现这张表中任何值的修改。

则在`AdminMapper.java`接口中添加抽象方法：

```java
int update(Admin admin);
```

然后，在`AdminMapper.xml`中配置：

```xml
<update id="update">
	UPDATE ams_admin
    <set>
    	<if test="username != null">
        	username=#{username},
        </if>
        <if test="password != null">
        	password=#{password},
        </if>
        
        ... ...
        
    </set>
    WHERE id=#{id}
</update>
```

完成后，在`AdminMapperTests.java`中编写并执行测试：

```java

```

# 启用或禁用管理员--Service层

当前业务，可以设计成1个业务，例如：

```java
void updateEnableById(Long id, Integer enable);
```

也可以设计成2个业务，例如：

```java
void setEnable(Long id); // enable固定设置为1
void setDisable(Long id); // enable固定设置为0
```

建议设计成2个业务，所以，在`IAdminService`中添加以上2个抽象方法。

然后，需要在`AdminServiceImpl`中实现以上2个抽象方法，这2个方法的实现应该是高度类似的，可以在内部声明私有的方法完成公共的实现：

```java
private void updateEnableById(Long id, Integer enable) {
    // 根据管理员id检查管理员数据是否存在
    
    // 检查管理员数据的当前状态是否与参数enable表示的状态相同
    
    // 创建Admin对象
    // 将方法的2个参数封装到Admin对象中
    // 调用AdminMapper对象的update()方法执行修改
}
```

则需要在Mapper层补充“根据管理员id查询管理员详情”的功能！





