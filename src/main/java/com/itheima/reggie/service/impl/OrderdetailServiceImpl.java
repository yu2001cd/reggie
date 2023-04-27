package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.mapper.OrderdetailMapper;
import com.itheima.reggie.service.OrderdetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderdetailServiceImpl extends ServiceImpl<OrderdetailMapper, OrderDetail> implements OrderdetailService {
}
