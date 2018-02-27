# Yobatis
Yobatis DAO是一款以Mybatis-Generator为核心的eclipse插件；可以按照DAO层形式生成Mybatis相关代码。如果项目是基于MySQL/Mybatis/SpringMVC/Servlet(tomcat)，该插件可生成Mybatis-Generator的配置文件，减少手写配置工作量。**表结构变更以后，只需要点击鼠标就可以更新相关代码，yobatis会保留手写部分的代码(java和xml)**。

 Yobatis会根据数据库表生成相应的DAO, domain, mybatis xml文件, 以及构造查询条件的类XxxCriteria。假设有一张book表:
 
<PRE>
+--------+------------+------+-----+---------+----------------+
| Field  | Type       | Null | Key | Default | Extra          |
+--------+------------+------+-----+---------+----------------+
| id     | bigint(20) | NO   | PRI | NULL    | auto_increment |
| name   | char(100)  | YES  |     | NULL    |                |
| author | bigint(20) | YES  |     | NULL    |                |
+--------+------------+------+-----+---------+----------------+
</PRE>
```
// 根据主键查询记录
@Override
@Transactional(rollbackFor = Exception.class)
public Book getById(Long id) {
  return bookDao.selectOne(id);
}
```
```
// 查询名字为name, 或作者是authorId的所有书籍: where (name = ?) or (author_id = ?) 
@Override 
@Transactional(rollbackFor = Exception.class) 
public List nameEqualOrAuthorIs(String name, long authorId) { 
  BookCriteria criteria = BookCriteria.nameEqualTo(name).or().andAuthorEqualTo(authorId); 
  return bookDao.selectList(criteria); 
}
```
# Requirements
* eclipse luna或者更高版本
* Java 8
* 仅支持MySQL
# 安装
单击Help菜单 -> 选择Install New Software -> 单击Add，对话框中添加repository: https://linbaiye.github.io/yobatis_upsite


![alt text](https://linbaiye.github.io/yobatis/img/install1.png)

![alt text](https://linbaiye.github.io/yobatis/img/install2.png)
# 生成代码
如果项目是基于MySQL/Mybatis/SpringMVC/Servlet(tomcat)，可以尝试使用yobatis自动生成配置文件，如下图：
![usage](https://linbaiye.github.io/yobatis/img/usage.gif)


生成配置文件后，可以手动更改生成代码的存放目录。若无法自动生配置文件，则需要手写配置文件（mybatisGeneratorConfig.xml），示例如下：
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN" "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
  <!-- ${user} 为用户目录, 该选项用于加载 mysql-connector-java-5.1.25.jar，需要正确配置 -->
  <classPathEntry location="${user}/.m2/repository/mysql/mysql-connector-java/5.1.25/mysql-connector-java-5.1.25.jar"/>
  <context id="org.nalby.yobatis.book.model" targetRuntime="MyBatis3">
    <!-- Yobatis 插件，必须 -->
    <plugin type="org.mybatis.generator.plugins.YobatisDaoPlugin"/>
    <jdbcConnection driverClass="com.mysql.jdbc.Driver" 
    connectionURL="jdbc:mysql://localhost:3306/book_store?characterEncoding=utf-8" 
    userId="root" password="root"/>
    <javaTypeResolver>
      <property name="forceBigDecimals" value="false"/>
    </javaTypeResolver>
    <!-- model, criteria class文件配置 -->
    <javaModelGenerator targetPackage="org.nalby.yobatis.book.model"
    targetProject="/yobatis-simple-example/src/main/java"/>
    <!-- xml mapper文件配置 -->
    <sqlMapGenerator targetPackage="mybatis-mappers"
    targetProject="/yobatis-simple-example/src/main/resources"/>
    <!-- dao层class文件配置 -->
    <javaClientGenerator type="XMLMAPPER"
    targetPackage="org.nalby.yobatis.book.dao" targetProject="/yobatis-simple-example/src/main/java"/>
    <!-- 需要生成对应代码的表 -->
    <table tableName="book" schema="book_store" modelType="flat">
      <generatedKey column="id" sqlStatement="mysql" identity="true"/>
    </table>
  </context>
</generatorConfiguration>
```


