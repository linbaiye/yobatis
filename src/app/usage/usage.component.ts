import { Component, OnInit } from '@angular/core';
import {MethodUsage} from '../method-usage';

@Component({
  selector: 'app-usage',
  templateUrl: './usage.component.html',
  styleUrls: ['./usage.component.css']
})
export class UsageComponent implements OnInit {

  methodUsageList: MethodUsage[] = [
    new MethodUsage('int insert(BaseEmployee record)', '插入一条记录',
      '调用该方法向表中插入一条记录，如表为自增主键且record中对应主键的field为null，该方法会将新主键设置到record对应的field中；如表为自增主键且record中对应主键的field不为null，则将对应的主键插入表中。',
      '返回1若插入成功。', `
Employee employee = new Employee();
employee.setName('Alice');
employee.setPhone('123');
employeeDao.insert(employee);
System.out.println('新纪录的id是:' + employee.getId());

employee = new Employee();
employee.setName('Bob');
employee.setPhone('124');
employee.setId(2L);
employeeDao.insert(employee); // 新记录的id(主键)为2`),
    new MethodUsage('Employee selectOne(Long pk)', '通过主键查询一条记录', '根据主键查询记录。',
      '返回记录对应的对象或者null。', `
Employee employee = employeeDao.selectOne(1L);
if (employee == null) {
  System.out.println('没有id为1的员工.');
} else {
  System.out.println('员工的信息为:' + employee.toString());
}`),
    new MethodUsage('Employee selectOne(EmployeeCriteria criteria)', '通过criteria查询一条记录', '通过criteria查询一条记录。',
      '返回记录对应的对象或者null，若查询条件命中多条记录则抛出异常。', `
try {
  Employee employee = employeeDao.selectOne(EmployeeCriteria.nameEqualTo("Alice"));
  if (employee == null) {
    System.out.println("没有名字为Alice的记录");
  } else {
    System.out.println("找到一条名字为Alice的记录:", employee.toString());
  }
} catch (TooManyResultsException e) {
  System.out.println("找到多条名字为Alice的记录");
}`),
    new MethodUsage('List<Employee> selectList(EmployeeCriteria criteria)', '通过criteria查询多条记录', '通过criteria查询多条记录。',
      '返回查询到的记录，或者一个空List如果没有查询到相应记录。', `
// where name = 'Alice' and phone is not null;
List<Employee> list = employeeDao.selectList(EmployeeCriteria.nameEqualTo("Alice").andPhoneIsNotNull());
// 该方法不会返回null
for (Employee employee : list) {
  System.out.println(employee.toString());
}

// where (name = 'Alice' and phone is not null) or (name = 'Bob')
List<Employee> list = employeeDao.selectList(EmployeeCriteria.nameEqualTo("Alice").andPhoneIsNotNull()
		.or() // <-加入or
		.andNameEqualTo("Bob"));
for (Employee employee : list) {
  System.out.println(employee.toString());
}

// select id, name, phone from employee where id is not null limit 10 offset 1 order by name asc, phone desc for update
List<Employee> list = employeeDao.selectList(EmployeeCriteria.idIsNotNull()
  .setLimit(10L) // 最多10条记录
  .setOffset(1L) // offset为1
  .ascOrderBy("name") // 通过name字段做asc排序, 参数为数据库中的字段名
  .descOrderBy("phone") // 若name相同则通过phone desc排序, 参数为数据库中的字段名
  .setForUpdate(true) // 设置for update, 小心使用
);
for (Employee employee : list) {
  System.out.println(employee.toString());
}
`),
    new MethodUsage('int count(EmployeeCriteria criteria)', '统计符合criteria的记录数量', '通过criteria统计记录数量。',
      '返回统计数量。', `
int count = employeeDao.count(EmployeeCriteria.phoneIsNotNull());
System.out.println("找到" + count + "电话不为空的记录");`),
    new MethodUsage('int update(BaseEmployee record)', '通过主键更新记录', '该方法将record中不为null的field更新到对应主键的记录中',
      '返回1如果更新成功，0如果该记录不存在', `
Employee employee = new Employee();
employee.setId(2L);
// 只更新phone, name保持现状
employee.setPhone("156");
int count = employeeDao.update(employee);
if (count == 1) {
  System.out.println("更新成功。");
} else {
  System.out.println("没有找到id为2的记录");
}`),
    new MethodUsage('int update(BaseEmployee record, EmployeeCriteria criteria)', '通过criteria批量更新记录',
      '该方法将record中不为null的field更新到criteria选中的记录。',
      '返回被更新记录数量。', `
Employee employee = new Employee();
employee.setPhone("156");
// update employee set phone = '156' where id <= 4
int count = employeeDao.update(employee, EmployeeCriteria.idLessThanOrEqualTo(4L));
System.out.println("成功更新" + count + "条记录");`),
    new MethodUsage('int delete(Long pk)', '删除主键对应的记录', '删除主键对应的记录。',
      `返回1如果删除成功，0如果没有该记录。`, `
// delete from employee where id = 1
int count = employeeDao.delete(1L);
if (count == 1) {
  System.out.println("删除成功");
} else {
  System.out.println("没有找到该记录");
}`),
    new MethodUsage('int delete(EmployeeCriteria criteria)', '通过条件批量删除', '批量删除，小心使用。',
      '被删除记录数量。', `
// delete from employee where id is not null
// 务必保证这是你想要的。
int count = employeeDao.delete(EmployeeCriteria.idIsNotNull());
System.out.println("一共删除" + count + "条记录");`)];

  constructor() { }

  ngOnInit() {
  }

}
