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

每个Spring Bean都有一个名字，如果是通过组件扫描的做法创建的Bean，当类名的首字母是大写的，且第2个字母是小写，则此Spring Bean的名称就是将类名的首字母改为小写，例如`AdminController`的Bean Name就是`adminController`，如果不满足首字母大写且第2字母小写的规则，则Spring Bean的名称就是类名；如果是通过`@Bean`方法的做法创建的Bean，则Bean Name就是方法名称。

## Spring Bean的作用域

默认情况下，Spring Bean都是单例的！如果你不希望某个Spring Bean是单例的，可以使用`@Scope("prototype")`，当使用组件扫描的做法时，在组件类上添加此注解，当使用`@Bean`方法的做法时，在方法上添加此注解。

> 单例：在任何时刻，此类的对象最多只有1个。
>
> 提示：单例的对象都是常驻内存的，即从创建出来的那一刻，直到程序运行结果，此对象一直在内存中。
>
> 提示：Spring并没有使用单例模式，只是Spring Bean具有单例的表现特征。

对于单例的Spring Bean，默认情况下是**预加载**的，如果你不希望某个Spring Bean是预加载的，可以使用`@Lazy`注解，当使用组件扫描的做法时，在组件类上添加此注解，当使用`@Bean`方法的做法时，在方法上添加此注解，会使得Spring Bean是**懒加载**的。



















```java
static
```

