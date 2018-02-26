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
# BUG & 改进
使用中遇到问题，或者有改进建议请提交issue。
