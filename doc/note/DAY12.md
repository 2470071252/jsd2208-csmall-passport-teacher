# 关于Spring框架

## Spring框架的作用

Spring框架主要解决了创建对象、管理对象的相关问题。

## Spring框架的依赖项

当在项目中需要使用Spring框架时，需要添加`spring-context`依赖项，例如：

```xml
<!-- https://mvnrepository.com/artifact/org.springframework/spring-context -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.3.20</version>
</dependency>
```

当然，在Spring Boot项目中，默认已经包含此依赖项（可能版本不是以上`5.3.20`，但这并不重要），其实，任何被实际应用的Spring Xxx框架都包含`spring-context`，例如Spring MVC、Spring JDBC、Spring Validation、Spring Security等。

## 通过Spring框架创建对象--组件扫描

在配置类上使用`@ComponentScan`注解，可以开启组件扫描，一旦这个配置类被加载，就会扫描特定的包及其子孙包中的所有类，如果扫描到的类是组件类，就会自动创建这些类的对象。

关于扫描的包，可以通过`@ComponentScan`注解的参数进行配置，例如配置为：

```java
@ComponentScan("cn.tedu.csmall.passport")
```

此注解的`value`参数是`String[]`类型的，所以，也可以指定多个需要扫描的包，例如：

```java
@ComponentScan({"cn.tedu.csmall.passport.controller", "cn.tedu.csmall.passport.service"})
```

如果使用`@ComponentScan`时没有配置注解参数，则扫描添加了此注解的配置类所在的包。

在Spring Boot项目中，启用类上添加了`@SpringBootApplication`注解，此注解的元注解就包括`@ComponentScan`且没有配置包名，所以，在Spring Boot中，是已经存在组件扫描机制的，且扫描的包就是启用类所在的包！

关于组件类，是添加了组件注解的类，在Spring框架中，典型的组件注解有：

- `@Component`：通用组件注解
- `@Repository`：添加在数据存储库类上的组件注解
- `@Service`：添加在业务逻辑类上的组件注解
- `@Controller`：添加在控制器类上的组件注解

在Spring框架的作用范围内，以上4个注解的使用方式、作用是完全等效的，只是语义不同！

在Spring框架中，还一个特殊的组件注解：`@Configuration`，添加了此注解的类将会被视为“配置类”，Spring框架会通过CGLib代理来处理此类（你可以不关心代理的实现特征）。

在基于Spring的Spring MVC中，提供了更多的组件注解：

- `@RestController`
- `@ControllerAdvice`
- `@RestControllerAdvice`

# 通过Spring框架创建对象--@Bean方法

在配置类中，可以自定义某个方法，此方法可以返回某个对象，然后，在方法上添加`@Bean`注解，则Spring加载此配置类时，就会自动执行此方法，从而实现创建对象的效果！

例如：

```java
@Configuration
public class XxxConfiguration {
    
    @Bean
    public AdminController adminController() {
        return new AdminController();
    }
    
}
```

## 通过Spring框架创建对象--其它

无论是组件扫描的做法，还是`@Bean`方法的做法，只要是由Spring执行的创建，被创建出来的对象都会被Spring保管起来，后续，可以通过Spring获取这些对象。

被Spring创建的对象通常称之为Spring Bean。

由于Spring会创建并保管若干个对象，并且，可以通过Spring获取这些对象，所以，也经常把Spring称之为“容器”。

在开发实践中，当需要Spring管理某个类的对象时，通常，如果这个类是自定义的，优先使用组件扫描的做法，如果这个类不是自定义的，只能使用`@Bean`方法的做法！

每个Spring Bean都有一个名字，如果是通过组件扫描的做法创建的Bean，当类名的首字母是大写的，且第2个字母是小写，则此Spring Bean的名称就是将类名的首字母改为小写，例如`AdminController`的Bean Name就是`adminController`，如果不满足首字母大写且第2字母小写的规则，则Spring Bean的名称就是类名，也可以通过组件注解来指定名称，例如`@Component("adminController")`；如果是通过`@Bean`方法的做法创建的Bean，则Bean Name就是方法名称，也可以通过注解参数来指定名称，例如`@Bean("adminController")`。

## Spring Bean的作用域

默认情况下，Spring Bean都是单例的！如果你不希望某个Spring Bean是单例的，可以使用`@Scope("prototype")`，当使用组件扫描的做法时，在组件类上添加此注解，当使用`@Bean`方法的做法时，在方法上添加此注解。

> 单例：在任何时刻，此类的对象最多只有1个。
>
> 提示：单例的对象都是常驻内存的，即从创建出来的那一刻，直到程序运行结果，此对象一直在内存中。
>
> 提示：Spring并没有使用单例模式，只是Spring Bean具有单例的表现特征。

对于单例的Spring Bean，默认情况下是**预加载**的，如果你不希望某个Spring Bean是预加载的，可以使用`@Lazy`注解，当使用组件扫描的做法时，在组件类上添加此注解，当使用`@Bean`方法的做法时，在方法上添加此注解，会使得Spring Bean是**懒加载**的。

## 自动装配机制

Spring的自动装配机制表现为：当类中的某个属性需要值时，或被Spring调用的方法的参数需要值时，通过特定的语法表现（通常是添加注解），Spring可以从容器中找出**合适的**对象，为属性或方法参数赋值。

典型表现是在需要值的属性上添加`@Autowired`注解，例如：

```java
@RestController
public class AdminController {
    
    @Autowired // 自动装配
    private IAdminService adminService;
    
}
```

> 提示：自动装配的前提是当前类是由Spring创建对象的！

另外，也可以通过`@Resource`注解实现同样的效果，即：

```java
@RestController
public class AdminController {
    
    @Resource // 自动装配
    private IAdminService adminService;
    
}
```

关于`@Autowired`和`@Resource`注解的区别：

- `@Resource`注解是`javax`包中的注解，是优先按照Bean Name查找合适的对象（要求Bean  Name与被装配的属性名相同），如果没有符合的，则按照类型查找合适的对象
- `@Autowired`注解是Spring框架的注解，是先在Spring容器中查找匹配类型的对象的数量，如果存在多个类型匹配的，将按照名称来匹配

无论使用以上哪种注解，如果存在同类型的多个Spring Bean，且名称与被装配的属性都不匹配，都会导致无法装配，项目启动失败。

在开发实践中，绝大部分被装配的属性，在Spring容器中都只有1个匹配类型的对象，所以，无论使用以上哪种注解，都是可以成功装配的。

在Spring框架中，默认的装配机制是`@Autowired`的机制，完整装配机制如下：

- 查询匹配类型的Bean的数量，结果可能是：
  - 0个：取决于`@Autowired`注解的`required`属性
    - `true`（默认）：无法装配，导致装配失败，启动项目时就会报错
    - `false`：放弃装配，则属性值为`null`，后续可能导致NPE
  - 1个：直接装配，且装配成功
  - 多个：将按照名称进行匹配，如果存在名称匹配的Spring Bean，则装配此Spring Bean，如果不存在，则装配失败，启用项目时就会报错

关于匹配Spring Bean的名称：

- 可以修改尝试被装配的属性名或方法的参数名，使之与某个Spring Bean的名称相同

- 可以修改Spring Bean的名称，使之与尝试被装配的属性名或方法的参数名相同

- 可以在尝试被装配的属性或方法的参数上添加`@Qualifier`注解，通过此注解的参数指定Spring Bean的名称，例如：

  ```java
  @Autowired
  @Qualifier("adminServiceImpl")
  private IAdminService adminService;
  ```

  > 提示：`@Qualifier`注解可以添加在属性上，也可以添加在方法的参数上。

被Spring调用的方法的参数需要值时，也是通过自动装配机制来实现为参数赋值的，此机制可完全参考`@Autowired`的装配机制。

例如，当Spring尝试创建一个通过组件扫描找到的组件类的对象时，会自动调用此组件类的构造方法，关于构造方法的调用：

- 如果类中仅有1个构造方法，无论它是无参数的，还是有参数的，Spring都会尝试调用它，如果是有参数，Spring会自动通过自动装配机制为此参数赋值
- 如果类中存在无参数的构造方法，无论还有没有其它构造方法，默认情况下，Spring都会自动调用无参数的构造方法



















```java
static
```

