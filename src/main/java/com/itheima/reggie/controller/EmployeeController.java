package com.itheima.reggie.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")//?
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping ("/login")
    /**
     * 根据页面提供username查询数据库
     * 返回值为空返回登录失败
     * 返回值密码错误返回登录失败
     * 员工状态为禁用返回结果为禁用
     * 登录成功，把员工id存入session
     */
    public R login(HttpServletRequest request, @RequestBody Employee employee){
        String password =employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());//md5加密
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<Employee>();
        lqw.eq(Employee::getUsername,employee.getUsername());//条件封装
        Employee emp = employeeService.getOne(lqw);
        if (emp==null){
            return R.error("用户名不存在");
        }
        if (emp.getStatus()==0){
            return R.error("该用户已被禁用");
        }
        if (!emp.getPassword().equals(password)){
            return R.error("密码不正确");
        }
        request.getSession().setAttribute("employee",emp.getId());//将员工id存入session
        return R.success(emp);
    }


    @PostMapping("/logout")
    /**
     * 退出登录
     */
    public R logout(HttpServletRequest httpServletRequest){
        httpServletRequest.getSession().removeAttribute("employee");//将员工id移出seesion
        return R.success("退出成功");
    }

    @PostMapping
    /**
     * 新增员工
     */
    public R save(HttpServletRequest request, @RequestBody Employee employee){
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));//设置密码
        /*Long createid = (Long)request.getSession().getAttribute("employee");//获取操作员工id
        employee.setCreateTime(LocalDateTime.now());//创建时间
        employee.setUpdateTime(LocalDateTime.now());//更新时间
        employee.setCreateUser(createid);//创建人
        employee.setUpdateUser(createid);//更新人*/
        employeeService.save(employee);
        return R.success("新增员工成功");
    }


    @GetMapping("/page")
    /**
     * 分页查询
     */
    public R<Page> pageSelect(int page,int pageSize,String name){
        log.info("页数{},每页长度{},名字{}",page,pageSize,name);
        Page pageinfo = new Page(page,pageSize);//构造分页构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();//构造条件构造器
        lqw.like(!StringUtils.isEmpty(name),Employee::getName,name);
        lqw.orderByDesc(Employee::getUpdateTime);//排序条件
        employeeService.page(pageinfo,lqw);//执行查询
        return R.success(pageinfo);
    }


    @PutMapping
    /**
     * 根据id修改
     */
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        /*Long updateId = (Long)request.getSession().getAttribute("employee");
        employee.setUpdateUser(updateId);//更新人
        employee.setUpdateTime(LocalDateTime.now());//更新时间*/
        employeeService.updateById(employee);
        return R.success("修改成功！");
    }

    @GetMapping("/{id}")
    /**
     * 根据id查询
     */
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id{}查询信息",id);
        Employee employee = employeeService.getById(id);
        if (employee==null){
            return R.error("没有查询到员工信息");
        }
        return R.success(employee);
    }
}
