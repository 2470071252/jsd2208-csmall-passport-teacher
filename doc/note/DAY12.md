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







```java
User user = new User();
```

