package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.injector.methods.DeleteById;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    //查询套餐和菜品分类
    @GetMapping("/page")
    public R select(Integer page,Integer pageSize){
        log.info("执行分类分页查询页码{},每页长度{}",page,pageSize);
        Page categoryPage = new Page(page,pageSize);
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort);
        categoryService.page(categoryPage,lqw);//执行查询
        return R.success(categoryPage);
    }


    //新增套餐和菜品分类
    @PostMapping
    public R insert(@RequestBody Category category){
        categoryService.save(category);
        return R.success("新增成功！");
    }

    //根据id删除
    @DeleteMapping
    public R deleteById(Long ids){
        log.info("通过id删除，id为{}",ids);
        categoryService.removeById(ids);
        return R.success("删除成功");
    }

    //修改
    @PutMapping
    public R update(@RequestBody Category category){
        log.info("更新分类{}",category);
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    //下拉列表
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(category.getType()!=null,Category::getType,category.getType());
        lqw.orderByAsc(Category::getSort).orderByAsc(Category::getUpdateTime);
        List<Category> list = categoryService.list(lqw);
        return R.success(list);
    }
}
