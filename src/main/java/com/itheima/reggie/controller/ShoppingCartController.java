package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    //查看购物车
    @GetMapping("/list")
    public R list(){
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        lqw.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return R.success(list);
    }
    //添加菜品
    @PostMapping("/add")
    public R add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据{}",shoppingCart);
        //设置用户id
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //查询当前菜品或者套餐是否在购物车中 存在则数量加一 不存在则添加到购物车
        Long dishId =  shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,shoppingCart.getUserId());
        if (dishId!=null){//是菜品
            //用户相同 菜品相同
            lqw.eq(ShoppingCart::getDishId,dishId);
        } else {//是套餐
            //用户相同 套餐相同
            lqw.eq(ShoppingCart::getSetmealId, setmealId);
        }
        //查询
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(lqw);
        if (shoppingCart1!=null){//购物车中有了数量加1
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCart1.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(shoppingCart1);
        } else {//购物车中没有则加入购物车
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());//不能加注解因为只有creattime一个字段
            shoppingCartService.save(shoppingCart);
            shoppingCart1 = shoppingCart;
        }
        return R.success(shoppingCart1);
    }
    //减少菜品
    @PostMapping("/sub")
    public R sub(@RequestBody ShoppingCart shoppingCart){
        Long dishId =  shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        if (dishId!=null){//是菜品
            //用户相同 菜品相同
            lqw.eq(ShoppingCart::getDishId,dishId);
        } else {//是套餐
            //用户相同 套餐相同
            lqw.eq(ShoppingCart::getSetmealId, setmealId);
        }
        //查询
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(lqw);

        shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
        if (shoppingCart1.getNumber()==0){
            shoppingCartService.removeById(shoppingCart1.getId());
        } else {
            shoppingCartService.updateById(shoppingCart1);
        }
        return R.success(shoppingCart1);
    }
    //清空购物车
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(lqw);
        return R.success("删除成功");
    }
}
