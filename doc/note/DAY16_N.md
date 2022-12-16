# Mybatis中的占位符（续）

在**非Spring Boot项目**中，使用Mybatis时，如果抽象方法的参数有多个，且不想使用`arg?`或`param?`这样的参数名称，应该在抽象方法中的参数列表中使用`@Param`注解，以显式的指定参数名称，例如：

```java
int updateNameById(@Param("id") Long id, @Param("name") String name);
```

由于以上配置在注解中的是属性值，所以，即使编译，也不会丢失此值！Mybatis会自动匹配这里的名称，在配置SQL语句时，也应该使用`@Param`注解配置的名称作为占位符的名称，例如：

```java
int updateNameById(@Param("id") Long aaa, @Param("name") String bbb);
```

```xml
<update id="updateNameById">
    update
        pms_album
    set
        name=#{name}
    where
        id=#{id}
</update>
```

为了保证代码良好的兼容性，即使使用Spring Boot项目，在Mapper接口中的抽象方法有多个参数时，仍建议使为方法的每个参数添加`@Param`注解以配置参数名称。

# Mybatis中的`#{}`占位符与`${}`占位符

在使用Mybatis时，配置的SQL语句中的参数可以使用`#{}`格式的占位符，也可以使用`${}`格式的占位符。

在“根据id查询管理员信息”的Mapper功能中，使用`#{}`或`${}`格式的占位符，都可以正常执行查询，配置的SQL语句大致是：

```xml
<select id="getStandardById" resultMap="StandardResultMap">
    SELECT
        <include refid="StandardQueryFields"/>
    FROM
        ams_admin
    WHERE
        id=#{id}
</select>
```

在“根据用户名查询管理员信息”的Mapper功能中，使用`#{}`格式的占位符，可以正常执行查询，配置的SQL语句大致是：

```xml
<select id="getLoginInfoByUsername" resultMap="LoginInfoResultMap">
    SELECT
        <include refid="LoginInfoQueryFields"/>
    FROM
        ams_admin
    WHERE
        username=#{username}
</select>
```

以上配置中，如果将占位符换成`${}`格式的，执行查询时将出现以下错误：

```
### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'root' in 'where clause'
; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column 'root' in 'where clause'
```

以上错误信息中的核心部分在于：`Unknown column 'root' in 'where clause'`，表示：`在Where子句的root这一列是未知的（即列名不识别）`。

其实，执行测试时传入的参数`root`表示的是`username`这一列的值，却被当成了“列的名称”！因为，在SQL语句中，预定义的关键词、预定义的标点符号、数值、布尔值都是可以被正确区分，即MySQL知道它们表示的是什么意义，其它直接写出来的名称，都会被当成数据库名称、数据表名称、字段名（查询时称之为列名）等其它设计时定义的名称，例如：

```mysql
select * from ams_admin where username=root
```

以上SQL语句执行也会出现`Unknown column 'root' in 'where clause'`错误，因为`root`被当作列名了，但是`ams_admin`表中并没有声明这个字段，所以查询结果集中没有此列！

如果希望MySQL不会误以为某个值是某个名称，需要在值的两端添加一对单引号，例如：

```mysql
select * from ams_admin where username='root'
```

其实，严格意义上，在SQL语句中所有的值都应该添加一对单引号，只是因为数值、布尔值是可以直接识别的，一般应用时，数值和布尔值不会添加单引号。

在使用Mybatis时，SQL语句中的`${}`格式的占位符是先拼接（使用参数值替换占位符），再编译并执行，例如，当配置的SQL语句是：

```mysql
select * from ams_admin where username=${username}
```

假设传入的参数是`root`，则会把参数值拼到到SQL语句的占位符位置：

```mysql
select * from ams_admin where username=root
```

然后，尝试编译并执行此SQL语句！（以上SQL会出错）

要解决以上查询出错的问题，可以在SQL语句的`${username}`的两端添加单引号，或在传入的参数值的两端添加单引号。

SQL语句中的`#{}`格式的占位符是先编译，然后将再值代入并执行，不会出现以上问题。

SQL语句在被编译之前，需要先执行词法分析、语义分析，这些分析都顺利通过才会执行编译，所以，一旦成功编译，SQL语句的语义就已经确定下来了，那么，无论执行过程中传入什么的值，MySQL都会把它当成值，而不会误解为某个名称！

========================================================================

**【阶段小结】**使用`${}`格式的占位符，是先将参数值替换掉占位符，再执行词法分析、语义分析、编译、执行；使用`#{}`格式的占位符，是先执行词法分析、语义分析、编译，然后再将参数值代入到编译好的SQL语句中执行，这种做法也称之为**预编译**的。

========================================================================

当使用`${}`格式的占位符时，由于是先传值，再执行（词法分析）语义分析，所以，传入的值是**可能**改变原SQL语句的语义的！则**存在SQL注入的风险**！

当使用`#{}`格式的占位符时，由于是执行（词法分析）语义分析，再将值代入执行，由于语义在执行之前已经固定，所以，传入的值是**不可能**改变原SQL语句的语义的！则**完全不存在SQL注入的风险**！

========================================================================

**【阶段小结】**使用`#{}`格式的占位符没有SQL注入的风险，是安全的，使用`${}`格式的占位符存在SQL注入的风险。

========================================================================

其实，`#{}`格式的占位符虽然有许多优点，但是使用也是受限的，它只能用于表示某个值，不可以用于表示SQL语句中的其它片段，但`${}`是不受限制的，只要将值替换掉占位符后的SQL语句是合法的，就可允许的，使用更加灵活！



```mysql
select * from user where ${xx}

id=9527
username='root'
username='liucangsong' and password='123456'
id>=0 order by id desc


```

**【SQL注入示例】**

```mysql
select * from user where username=? and password=?
								  root           hello
								  xxxx           hello or 1=1

select * from user where username='?' and password='?'
								  root              hello
								  xxxx              hello' or '1'='1
								  
select * from user where username='?' and password='hello' or '1'='1'
```















