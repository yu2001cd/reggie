package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    //新增套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功!");
    }
    //分页查询套餐
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page,int pageSize,String name){
        //分页查询
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null,Setmeal::getName,name);
        lqw.orderByAsc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage,lqw);
        //将分页查询的结果中的records获取并赋值给新的records 新的records有套餐类型字段
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> setmealDtos = records.stream().map((item -> {
            SetmealDto setmealDto = new SetmealDto();
            //查询套餐分类字段
            String categoryname = categoryService.getById(item.getCategoryId()).getName();
            //把旧records的信息赋值给新records
            BeanUtils.copyProperties(item, setmealDto);
            setmealDto.setCategoryName(categoryname);
            return setmealDto;
        })).collect(Collectors.toList());
        //旧page的值除records以外赋值给新page 并且给新page设置新records
        Page<SetmealDto> setmealDtoPage = new Page<>();
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");
        setmealDtoPage.setRecords(setmealDtos);
        return R.success(setmealDtoPage);
    }

    //删除套餐
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithDish(ids);
        return R.success("删除套餐成功");
    }

    //展示套餐内容
    @GetMapping("/list")
    public R<List<Setmeal>> list (Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lqw =new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(Setmeal::getStatus,"1");
        lqw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lqw);
        return R.success(list);
    }
    //套餐起售
    @PostMapping("status/1")
    public R<String> sale(@RequestParam List<Long> ids){
        log.info("修改套餐数据为{}",ids);
        //查出需要修改的套餐
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,0);
        List<Setmeal> list = setmealService.list(lqw);
        list.stream().map((item ->{
            item.setStatus(1);
            return item;
        })).collect(Collectors.toList());
        setmealService.updateBatchById(list);
        return R.success("修改成功");
    }
    //套餐禁售
    @PostMapping("status/0")
    public R<String> dissale(@RequestParam List<Long> ids){
        log.info("修改套餐数据为{}",ids);
        //查出需要修改的套餐
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);
        List<Setmeal> list = setmealService.list(lqw);
        list.stream().map((item ->{
            item.setStatus(0);
            return item;
        })).collect(Collectors.toList());
        setmealService.updateBatchById(list);
        return R.success("修改成功");
    }
    //展现单个套餐
    @GetMapping("/{id}")
    public R<Setmeal> getbyId(@PathVariable Long id){
        log.info("展现的套餐id为{}",id);
        return R.success(setmealService.getById(id));
    }
    //修改套餐
    @PutMapping
    public R<String> update(@RequestBody Setmeal setmeal){
        setmealService.updateById(setmeal);
        return R.success("修改成功");
    }
}
