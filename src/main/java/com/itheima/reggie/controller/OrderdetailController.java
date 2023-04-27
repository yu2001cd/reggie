package com.itheima.reggie.controller;

import com.itheima.reggie.service.OrderdetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/orderdetail")
public class OrderdetailController {
    @Autowired
    private OrderdetailService orderdetailService;
}
