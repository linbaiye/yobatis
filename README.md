# Yobatis简介

Yobatis是一款基于MybatisGenerator的IDEA插件，在MG的基础上做了一些封装，快速生成基础的CURD方法。插件以Tool Window的形式呈现，只需要简单配置好数据库和文件生成路径即可: <br>
<img src="src/assets/img/activation.jpg" width=300px height=300px />
<img src="src/assets/img/generate.jpg" width=300px height=300px />
<br>
按照上图配置好单击Generate就可以生成代码:<br>
<img src="src/assets/img/before_generation.jpg" width=300px height=350px /> ----->
<img src="src/assets/img/after_generation.jpg" width=300px height=350px />

## 使用生成代码:
生成代码使用简单，比如:

```
// 通过条件查询： where name = 'Alice' and phone is not null
List<Employee> list = employeeDao.selectList(EmployeeCriteria.nameEqualTo("Alice").andPhoneIsNotNull());
// 该方法不会返回null
for (Employee employee : list) {
  System.out.println(employee.toString());
}
```


