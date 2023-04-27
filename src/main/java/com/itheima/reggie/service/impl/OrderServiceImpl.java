package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderSercie {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderdetailService orderdetailService;
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userid = BaseContext.getCurrentId();
        //查询当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userid);
        List<ShoppingCart> list = shoppingCartService.list(lqw);
        if (list==null){
            throw new CustomException("购物车为空,不能下单");
        }
        //查询用户
        User user = userService.getById(userid);
        //查询地址
        Long addressId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressId);
        if (addressId==null){
            throw new CustomException("地址信息有误,不能下单");
        }
        //向订单表插入数据
        long orderid = IdWorker.getId();//订单号

        AtomicInteger amout = new AtomicInteger(0);
        //一个菜品对应一条订单明细数据
        List<OrderDetail> orderDetails = list.stream().map((item ->{
            OrderDetail  orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderid);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amout.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        })).collect(Collectors.toList());

        orders.setNumber(String.valueOf(orderid));
        orders.setId(orderid);
        orders.setUserId(userid);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amout.get()));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName()==null?"":addressBook.getProvinceName())
                            +(addressBook.getCityName()==null?"":addressBook.getProvinceName())
                            +(addressBook.getDistrictName()==null?"":addressBook.getDistrictName())
                            +(addressBook.getDetail()==null?"":addressBook.getDetail()));
        this.save(orders);
        //向明细表插入数据，多条
        orderdetailService.saveBatch(orderDetails);
        //清空购物车
        shoppingCartService.remove(lqw);
    }
}
