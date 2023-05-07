package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;


    @ApiOperation("新增套餐接口")
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//删除redis中的套餐数据
    /**
     * 后台新增套餐
     */
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功!");
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",required = true),
    })
    /**
     * 后台分页查询套餐
     */
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


    @DeleteMapping
    /**
     * 后台删除套餐
     */
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithDish(ids);
        return R.success("删除套餐成功");
    }


    @GetMapping("/list")
    /**
     * 展示套餐内容
     */
    @Cacheable(value = "setmealCache",key="#setmeal.categoryId+'_'+#setmeal.status")//把查询到套餐添加到redis
    public R<List<Setmeal>> list (Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lqw =new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(Setmeal::getStatus,"1");
        lqw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lqw);
        return R.success(list);
    }

    @PostMapping("status/1")
    /**
     * 后台套餐起售
     */
    @CacheEvict(value = "setmealCache",allEntries = true)//删除redis中的套餐数据
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

    @PostMapping("status/0")
    @CacheEvict(value = "setmealCache",allEntries = true)//删除redis中的套餐数据
    /**
     * 后台套餐禁售
     */
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


    @GetMapping("/{id}")
    /**
     * 展现单个套餐(修改套餐时使用)
     */
    public R<Setmeal> getbyId(@PathVariable Long id){
        log.info("展现的套餐id为{}",id);
        return R.success(setmealService.getById(id));
    }


    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//删除redis中的套餐数据
    /**
     * 修改套餐
     */
    public R<String> update(@RequestBody Setmeal setmeal){
        setmealService.updateById(setmeal);
        return R.success("修改成功");
    }
}
