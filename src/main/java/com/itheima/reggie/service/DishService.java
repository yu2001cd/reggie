package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    public void saveWithFlavors (DishDto dishDto);
    public DishDto getByIdWithFlavors(Long id);

    public void updateWithFlavors(DishDto dishDto);

    public void deleteWithFlavors(List<Long> ids);
}
