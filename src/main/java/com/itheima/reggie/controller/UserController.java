package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    //发送验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("生成验证码{}",code);
            //调用阿里云服务发送短信验证码
            /*SMSUtils.sendMessage("瑞吉外卖","",phone,code);*/
            //将生成的验证码保存到session
            session.setAttribute(phone,code);
            return R.success("验证码发送成功");
        }
        return R.error("验证码发送失败");
    }
    //登录
    @PostMapping("/login")
    public R<User> sendMsg(@RequestBody Map user, HttpSession session){
        log.info("登录信息为{}",user);
        String phone = (String) user.get("phone");
        String code = (String) user.get("code");
        //获取手机号对应的验证码
        Object codeinSession = session.getAttribute(phone);
        if (codeinSession!=null && codeinSession.equals(code)){
            //查询用户有没有注册
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone,phone);
            User user1 = userService.getOne(lqw);
            //如果用户没有注册，那么注册此用户
            if (user1==null){
                user1 = new User();
                user1.setPhone(phone);
                userService.save(user1);
            }
            session.setAttribute("user",user1.getId());
            return R.success(user1);
        }
        return R.error("登录失败");
    }
}
