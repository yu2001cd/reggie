package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐信息
        this.save(setmealDto);
        //保存菜品信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //为setmealDish赋上setmealId值
        setmealDishes.stream().map((item ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        })).collect(Collectors.toList());
        //保存setmealDish
        setmealDishService.saveBatch(setmealDishes);
    }
    @Transactional
    @Override
    public void deleteWithDish(List<Long> ids) {
        //先判断出售状态，如果在售不能删除
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,"1");
        int count = this.count(lqw);
        if (count>0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //删除套餐菜品关系表
        LambdaQueryWrapper<SetmealDish> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lqw2);
        /*//删除套餐分类关系表
        for (Long id : ids) {
            Setmeal setmeal = this.getById(id);
            Long categoryId = setmeal.getCategoryId();
            categoryService.removeById(categoryId);
        }*/
        //删除套餐
        this.removeByIds(ids);
    }
}
