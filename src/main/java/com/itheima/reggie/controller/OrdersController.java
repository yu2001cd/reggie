package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderSercie;
import com.itheima.reggie.service.OrderdetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {
    @Autowired
    private OrderSercie orderSercie;
    @Autowired
    private OrderdetailService orderdetailService;

    @PostMapping("/submit")
    /**
     * 用户下单
     */
    public R<String> submit(@RequestBody Orders orders){
        orderSercie.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/userPage")
    /**
     * 查询订单
     */
    public R<Page<OrdersDto>> getorders(int page,int pageSize){
        //查询用户对应的订单
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getUserId, BaseContext.getCurrentId());
        lqw.orderByAsc(Orders::getOrderTime);
        orderSercie.page(ordersPage,lqw);
        //将分页订单封装成含订单详情的分页订单
        Page<OrdersDto> ordersDtoPage = new Page<>();
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        List<Orders> records = ordersPage.getRecords();
        //将分页查询的订单内容改写为含订单详情的订单内容
        List<OrdersDto> ordersDtoList= records.stream().map((item ->{
            OrdersDto ordersDto  =new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            Long orderId = ordersDto.getId();
            //查询对应的订单详情
            LambdaQueryWrapper<OrderDetail> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(OrderDetail::getOrderId,orderId);
            List<OrderDetail> orderDetailListlist = orderdetailService.list(lqw2);
            ordersDto.setOrderDetails(orderDetailListlist);
            return ordersDto;
        })).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtoList);

        return R.success(ordersDtoPage);
    }

    @GetMapping("/page")
    /**
     * 后台查询订单
     */
    public R<Page<Orders>> orders(int page, int pageSize, String number,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime){
        log.info("查询条件为号码{},起始时间{},结束时间{}",number,beginTime,endTime);
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        if (number!=null){
            lqw.like(Orders::getNumber,number);
        }
        if (beginTime!=null&&endTime!=null){
            lqw.gt(Orders::getOrderTime,beginTime).lt(Orders::getOrderTime,endTime);
        }
        orderSercie.page(ordersPage,lqw);
        return R.success(ordersPage);
    }

    @PutMapping
    /**
     * 后台订单状态改变
     */
    public R<String> diliver(@RequestBody Orders orders){
        Orders orders1 = orderSercie.getById(orders.getId());
        orders1.setStatus(orders.getStatus());
        orderSercie.updateById(orders1);
        return R.success("修改成功");
    }
}
