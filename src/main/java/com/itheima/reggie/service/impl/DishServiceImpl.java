package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;
    //新增菜品 需要同时操作两张表 菜品表和口味表
    @Transactional
    public void saveWithFlavors (DishDto dishDto){
        this.save(dishDto);
        Long id = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        //给flavors赋上菜品id值
        flavors.stream().map((item ->{
            item.setDishId(id);
            return item;
        })).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
    //查询菜品 需查询菜品表和口味表
    @Override
    public DishDto getByIdWithFlavors(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        //查询菜品对应的口味
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(lqw);
        dishDto.setFlavors(list);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavors(DishDto dishDto) {
        //更新菜品表
        this.updateById(dishDto);
        List<DishFlavor> flavors = dishDto.getFlavors();
        //删除菜品对应的口味
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);
        //插入修改后的口味
        flavors.stream().map((item ->{
            item.setDishId(dishDto.getId());
            return item;
        })).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Transactional
    @Override
    public void deleteWithFlavors(List<Long> ids) {
        //先判断出售状态，如果在售不能删除
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId,ids);
        lqw.eq(Dish::getStatus,"1");
        int count = this.count(lqw);
        if (count>0){
            throw new CustomException("菜品正在售卖中，不能删除");
        }
        //如果可以删除 先删除菜品
        this.removeByIds(ids);
        //删除菜品对应的套餐关系表
        LambdaQueryWrapper<SetmealDish> lqw3 = new LambdaQueryWrapper<>();
        lqw3.in(SetmealDish::getDishId,ids);
        setmealDishService.remove(lqw3);
        //删除菜品口味关系表
        LambdaQueryWrapper<DishFlavor> lqw4 = new LambdaQueryWrapper<>();
        lqw4.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(lqw4);
    }
}
