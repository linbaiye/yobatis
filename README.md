# Yobatis
Yobatis是一个基于mybatis-generator的eclipse插件。如果你在使用spring, servlet, mysql开发web项目，Yobatis可以根据数据库帮你生成Mybatis-Generator配置文件, 以及dao和model的代码，减少手写Mybatis-Generator配置文件的工作量。
# Requirements
* eclipse luna或者更高版本
* Java 8
# 安装
单击Help菜单 -> 选择Install New Software -> 单击Add
![alt text](https://linbaiye.github.io/yobatis/img/install1.png)
添加repository:
https://linbaiye.github.io/yobatis_upsite
![alt text](https://linbaiye.github.io/yobatis/img/install2.png)
# 使用
用yobatis-simple-example作为示例，展示如何使用Yobatis
1. git clone https://github.com/linbaiye/yobatis-simple-example.git
2. 使用book_store.sql建表。
3. eclipse打开项目(maven项目)。
4. 在pom.xml文件中配置好数据库，初始配置如下:
```
<jdbc.username>root</jdbc.username>
<jdbc.password>root</jdbc.password>
<jdbc.url>jdbc:mysql://localhost:3306/book_store?characterEncoding=utf-8</jdbc.url>
<jdbc.driverClassName>com.mysql.jdbc.Driver</jdbc.driverClassName>
```
5.右键单击项目，选择弹出菜单中的"Yobatis"生成Mybatis-Generator配置文件"mybatisGeneratorConfig.xml"；右键单击该文件选择弹出菜单中的"Yobatis"生成代码。
![alt text](https://linbaiye.github.io/yobatis/img/usage.gif)
