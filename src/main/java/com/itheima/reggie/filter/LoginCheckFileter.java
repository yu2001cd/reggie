package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Slf4j
@WebFilter(filterName = "loginCheckFileter",urlPatterns = "/*")
public class LoginCheckFileter implements Filter {
    //路径匹配器 支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        log.info("拦截到请求{}",httpServletRequest.getRequestURI());
        String requestURI = httpServletRequest.getRequestURI();
        //定义不需要处理的请求数据
        String[] urls = new String[]{
                "/employee/login",
                "/employee/loginout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };
        //判断是否需要拦截
        boolean check = check(urls,requestURI);
        if (check){
            log.info("本次请求不需要处理");
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        //判断登录状态
        if (httpServletRequest.getSession().getAttribute("employee")!=null){
            log.info("用户已登录id为{}",httpServletRequest.getSession().getAttribute("employee"));
            Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);//将id保存在线程中
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }

        //判断登录状态2
        if (httpServletRequest.getSession().getAttribute("user")!=null){
            log.info("用户已登录id为{}",httpServletRequest.getSession().getAttribute("user"));
            Long userId = (Long) httpServletRequest.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);//将id保存在线程中
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        //返回未登录
        log.info("用户未登录");
        httpServletResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    //路径匹配 检查本次请求是否需要放行
    public boolean check(String[] strings,String requestURI){
        for (String string : strings) {
            boolean match = PATH_MATCHER.match(string, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
