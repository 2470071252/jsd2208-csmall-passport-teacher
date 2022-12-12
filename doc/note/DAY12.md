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

## 通过Spring框架创建对象--@Bean方法

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
- 如果类中存在多个构造方法，当希望Spring调用特定的某个构造方法时，可以在此构造方法上添加`@Autowired`注解

另外，也可以在Setter方法上添加`@Autowired`注解，Spring也会自动调用它们，也能实现自动装配参数的值，从而为属性赋值的效果，例如：

```java
@RestController
public class AdminController {

    private IAdminService adminService;

    @Autowired // 添加注解，Spring会自动调用此方法，为参数自动装配值
    public void setAdminService(IAdminService adminService) {
        this.adminService = adminService;
    }
    
}
```

以上为属性自动装配的做法，也适用于`@Bean`方法。

【小结】

- 自动装配表现为：为属性赋值、为方法的参数赋值（需要方法是被Spring自动调用的）
- 自动装配的注解：`@Resource`、`@Autowired`，请记住它们的装配机制
- 当被自动装配的属性或参数在Spring容器中存在多个类型匹配的值时，可以组合使用`@Autowired`和`@Qualifier`
- 当属性需要被自动装配值时，可以有3种方法：
  - 在属性上添加`@Autowired`注解
  - 通过Setter方法注入值，即在Setter方法添加`@Autowired`注解
  - 通过构造方法注入值
- 请记住Spring框架调用构造方法的机制

## 关于IoC与DI

**IoC**：**I**nversion **o**f **C**ontrol，即**控制反转**，即：将对象的控制权交给框架

**DI**：**D**ependency **I**njection，即**依赖注入**，即：为对象的依赖项注入值，通常表现为自动装配

Spring通过DI完善了IoC。

## 其它

Spring最核心的内容，主要分为：Spring IoC、Spring AOP，其中，Spring AOP会在本阶段末期再讲。

# Spring MVC

## Spring MVC框架的作用

Spring MVC框架主要解决了接收请求、响应结果的相关问题（其实，并没有关注MVC中的M）。

> 提示：**MVC** = **M**odel + **V**iew + **C**ontroller，这是主流的开发项目的思想，它认为每个项目至少需要有这3个部分，才是一个完整的项目，其中，Controller是控制器，用于接收请求、响应结果，View是视图，早些年在不是前后端分离的项目中，服务器端在处理完请求后，应该向客户端响应某个页面，此页面就是视图，目前，主流的设计方案是前后端分离的，则服务器端不需要也不会处理视图，Model是数据模型，是一套相对固定的数据处理流程，在项目中表现为Service与Mapper。

## Spring MVC框架的依赖项

当在项目中需要使用Spring框架时，需要添加`spring-webmvc`依赖项，例如：

```xml
<!-- https://mvnrepository.com/artifact/org.springframework/spring-webmvc -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>5.3.20</version>
</dependency>
```

## 关于控制器

在Spring MVC框架中，添加了`@Controller`注解的类，才算是控制器类！例如：

```java
@Controller
public class AdminController {
}
```

在控制器类中，可以添加处理请求的方法，并在方法上添加`@RequestMapping`系列注解来配置请求路径，例如：

```java
@Controller
public class AdminController {
    @PostMapping("/delete")
    public String delete() {
        // 暂不关心方法体
    }
}
```

以上代码处理请求之后，将返回一个`String`类型的结果（以上方法的返回值是`String`），这个返回值表示的是“视图组件的名称”，接下来，会由Spring MVC框架的其它组件根据这个视图名称找到对应的视图组件（例如某个html页面），最后，会将此视图组件响应到客户端去！

在前后端分离的做法中，服务器端并不会处理视图，当处理完某个请求后，向客户端响应必要的数据即可，至于这些数据如何呈现出来，应该是由客户端软件负责的！

在处理请求的方法上添加`@ResponseBody`，则此方法的返回值就不再是视图组件的名称，而是要响应到客户端的数据！所以，此注解也称之为“响应正文”的注解，例如：

```java
@Controller
public class AdminController {
    @PostMapping("/delete")
    @ResponseBody
    public String delete() {
        // 暂不关心方法体
    }
}
```

另外，也可以将此注解添加在控制器类上，则默认情况下，此类中所有处理请求的方法都将响应正文！例如：

```java
@Controller
@ResponseBody
public class AdminController {
    @PostMapping("/delete")
    public String delete() {
        // 暂不关心方法体
    }
}
```

为了便于使用，Spring MVC框架还提供了`@RestController`注解，它使用了`@Controller`和`@ResponseBody`作为元注解，所以，同时具有这2个注解的效果，例如：

```java
// @Controller    // 不再需要
// @ResponseBody  // 不再需要
@RestController   // 同时具有@Controller和@ResponseBody的效果
public class AdminController {
    @PostMapping("/delete")
    public String delete() {
        // 暂不关心方法体
    }
}
```

其源代码为：

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
public @interface RestController {
    @AliasFor(
        annotation = Controller.class
    )
    String value() default "";
}
```

与此类似的还有`@RestControllerAdvice`，同时具有`@ControllerAdvice`和`@ResponseBody`的效果。

## Spring MVC框架的相关注解

- `@RequestMapping`：可以添加在控制器类上，也可以添加在处理请求的方法上，主要用于配置请求路径，还可以配置其它与请求相关的设置，例如通过此注解的`method`属性来限制请求方式
- `@PostMapping`：添加在处理请求的方法上，主要用于配置请求路径，及其它与请求相关的参数，是将请求方式限制为`POST`的`@RequestMapping`
- `@GetMapping`：与`@PostMapping`类似，区别在于限制请求方式为`GET`
- `@PutMapping`：与`@PostMapping`类似，区别在于限制请求方式为`PUT`
- `@DeleteMapping`：与`@PostMapping`类似，区别在于限制请求方式为`DELETE`
- `@RequestParam`：添加在处理请求的方法的参数上，用于：配置请求参数的名称、要求必须提交此参数（不允许为`null`）、设置请求参数的默认值（不提交时视为某值）
- `@PathVariable`：添加在处理请求的方法的参数上，当设计URL时使用了占位符，则必须在方法的参数上通过此注解来获取占位符对应的值
- `@RequestBody`：添加在处理请求的方法的POJO类型的参数上，添加此注解后，客户端提交的请求参数必须是对象格式的，如果未添加此注解，客户端提交的请求参数必须是FormData格式的
- `@ResponseBody`：参考前节说明
- `@RestController`：参考前节说明
- `@ExceptionHandler`：添加在处理异常的方法上
- `@ControllerAdvice`：添加在某个类上，则此类中特定的方法（例如处理异常的方法）将作用于整个项目每次处理请求的过程中
- `@RestControllerAdvice`：参考前节说明

# Spring Boot

## Spring Boot框架的作用

Spring Boot框架主要解决了依赖管理、自动配置的相关问题。

在开发实践中，需要使用到的依赖项（框架、各种工具，等等）比较多，如果依赖项A依赖于B，依赖项C也依赖于B，但是，它们依赖的依赖项B的版本却不同，则无法正常使用！Spring Boot提供了一系列的`spring-boot-starter-???`依赖项，这些依赖项中都包括了主流的相关依赖项，以`spring-boot-starter-web`为例，其中就包含了Spring MVC框架的核心依赖项`spring-webmvc`，也包含了响应JSON时需要使用到的`jackson-databind`，等等，并且，管理了这些依赖项的版本，以至于各个开发者只需要添加`spring-boot-starter-web`即可，由Spring Boot来决定其依赖的`spring-webmvc`、`jackson-databind`等依赖项的版本，并保证是兼容可用的！

在没有Spring Boot框架之前，每创建一个新的项目，或添加新的依赖，可能都需要做大量的配置，而各个不同的项目中，使用相同的依赖时，需要编写的配置可能是高度相似，甚至完全相同的！Spring Boot希望它是“**开箱即用**的（Out Of Box）”，它自动的处理掉了许多可预测的配置，同时，它是希望遵循“**约定大于配置**”的思想的，即：各开发者不必关心Spring Boot是如何配置的，只需要知道Spring Boot把哪些配置项配置成什么值即可，然后，开发者只需要按照这些配置值的“约定”去写代码就行！例如，Spring Boot将组件扫描的包配置为启用类所在的包，开发者只需要将各组件类声明在此包或其子孙包下即可，根本不需要关心Spring Boot在哪里或通过什么方式配置了组件扫描！

## Spring Boot框架的依赖项

当在项目中需要使用Spring Boot框架时，需要添加`spring-boot-starter`基础依赖项，例如：

```xml
<!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>2.5.9</version>
</dependency>
```

## Spring Boot的典型特征

- 任何以`spring-boot-starter`作为前缀的依赖项（例如`spring-boot-starter-test`、`spring-boot-starter-web`等），都使用`spring-boot-starter`作为基础依赖项
- 在`spring-boot-starter`中，包含了SLF4j日志框架的依赖项，所以，任何一个Spring Boot项目，都可以直接使用SLF4j日志框架
- Spring Boot项目默认创建了`src/main/resources/application.properties`配置文件，并且在启用项目时，自动读取此配置文件，所以，在此配置文件中的配置是默认读取的
- Spring Boot指定了一系列特定名称的配置属性，例如`spring.datasource.url`、`server.port`等，按照这些名称编写的配置，会被自动应用
- Spring Boot支持Profile配置，任何以`application-自定义名称.properties`作为文件名的配置文件，都会被视为Profile配置，例如`application-dev.properties`、`application-dev-jack.properties`等
- 在`spring-boot-starter`中，包含了`snakeyaml`工具包，可以解析YAML语法的配置，所以，在Spring Boot项目中，可以使用`.yml`作为扩展名的配置文件，取代`.properties`配置文件
- Spring Boot项目的启用类上添加了`@SpringBootApplication`注解，每个项目中只能有1个类添加此注解
- 启用类上的`@SpringBootApplication`注解还使用了`@SpringBootConfiguration`作为元注解，且`@SpringBootConfiguration`使用`@Configuration`作为元注解，所以，启动类本身也是一个配置类
- 启用类上的`@SpringBootApplication`注解还使用了`@ComponentScan`作为元注解，且没有配置组件的包，所以，默认执行组件扫描时，会扫描启用类所在的包
- 在`src/test/java`下编写的测试类，当需要加载项目的各种配置和Spring环境时，需要在测试类上添加`@SpringBootTest`注解，且这样的测试类所在的包，与`src/main/java`下的组件扫描的包必须是一致的
- 当添加了数据库编程的相关依赖项时（例如`spring-boot-starter-jdbc`，或包括此依赖项的其它依赖，例如`mybatis-spring-boot-starter`中就包含了`spring-boot-starter-jdbc`），启用项目时，Spring Boot会自动读取连接数据库的配置（即从配置文件中读取`spring.datasource.url`属性的值），如果在配置文件中并未配置此属性，或此属性的值不是以`jdbc:`开头的，会导致项目启动失败
- 当添加了数据库编程的相关依赖项时，会自动配置`DataSource`对象
- 当添加了`spring-boot-starter-web`依赖项后，此依赖项中包含一个嵌入式的Tomcat，当启用项目时，会自动将当前项目部署到此Tomcat上并启用Tomcat，默认占用`8080`端口，可通过配置文件中的`server.port`属性修改端口号
- 在Spring MVC项目中，当需要响应JSON格式的正文时，需要添加`jackson-databind`依赖，在`spring-boot-starter-web`中也包含了依赖，所以，在基于Spring Boot的Web开发中，可以直接响应JSON格式的正文
- 当添加了`spring-boot-starter-web`依赖项后，会将`src/main/resources/static`配置为默认的静态资源文件夹，如果需要允许访问静态资源，将相关文件放在此文件夹中即可
  - 在创建项目时勾选了`Web`选项，会自动创建此`static`文件夹，如果未勾选，只要添加了依赖项，可自行创建此文件夹
  - 另外还有`src/main/resources/templates`文件夹也可作为静态资源文件夹，但不推荐，此文件夹应该是用于存放模板视图文件的（非前后端分离项目才需要使用）









