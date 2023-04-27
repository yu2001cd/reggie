package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    //新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavors(dishDto);
        return R.success("新增菜品成功");
    }
    //菜品分页查询
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Dish> dishPage = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null,Dish::getName,name);
        lqw.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage,lqw);
        //对象拷贝
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");//忽略record是因为新的records需要包含菜品分类名

        List<Dish> dishes = dishPage.getRecords();
        List<DishDto> dtodishs =  dishes.stream().map((item ->{
            DishDto dishDto = new DishDto();
            Long categroyId = item.getCategoryId();
            //根据id查询到分类对象
            Category category = categoryService.getById(categroyId);
            //给dishDto赋值
            BeanUtils.copyProperties(item,dishDto);
            dishDto.setCategoryName(category.getName());
            return dishDto;
        })).collect(Collectors.toList());

        dishDtoPage.setRecords(dtodishs);

        return R.success(dishDtoPage);
    }
    //通过id查询菜品信息和对应的口味信息
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavors(id);
        return R.success(dishDto);
    }

    //修改菜品及口味表
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavors(dishDto);
        return R.success("更新菜品成功");
    }

    //根据菜品类型查询菜品
    /*@GetMapping("/list")
    public R<List<Dish>> getCategoryDish(Dish dish){
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId,dish.getCategoryId());
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        lqw.eq(Dish::getStatus,"1");
        List<Dish> list = dishService.list(lqw);
        return R.success(list);
    }*/

    @GetMapping("/list")
    public R<List<DishDto>> getCategoryDish(Dish dish){
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId,dish.getCategoryId());
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        lqw.eq(Dish::getStatus,"1");
        List<Dish> list = dishService.list(lqw);
        List<DishDto> dishDtos = list.stream().map((item -> {
            DishDto dishDto = dishService.getByIdWithFlavors(item.getId());
            return dishDto;
        })).collect(Collectors.toList());
        return R.success(dishDtos);
    }
    //菜品起售
    @PostMapping("status/1")
    public R<String> sale(@RequestParam List<Long> ids){
        log.info("修改菜品数据为{}",ids);
        //查出需要修改的菜品
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId,ids);
        lqw.eq(Dish::getStatus,0);
        List<Dish> list = dishService.list(lqw);
        list.stream().map((item ->{
            item.setStatus(1);
            return item;
        })).collect(Collectors.toList());
        dishService.updateBatchById(list);
        return R.success("修改成功");
    }
    //菜品停售
    @PostMapping("status/0")
    public R<String> dissale(@RequestParam List<Long> ids){
        log.info("修改菜品数据为{}",ids);
        //查出需要修改的菜品
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId,ids);
        lqw.eq(Dish::getStatus,1);
        List<Dish> list = dishService.list(lqw);
        list.stream().map((item ->{
            item.setStatus(0);
            return item;
        })).collect(Collectors.toList());
        dishService.updateBatchById(list);
        return R.success("修改成功");
    }
    //菜品删除
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        dishService.deleteWithFlavors(ids);
        return R.success("删除套餐成功");
    }
}
