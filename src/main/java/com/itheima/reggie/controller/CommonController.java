package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")//从yml文件导入
    private String basePath;
    @PostMapping("/upload")
    /**
     * 上传图片
     */
    public R upload(MultipartFile file){
        //file是一个临时文件 需转存到指定位置 否则请求结束后会删除
        log.info(file.toString());
        String originalFilename = file.getOriginalFilename();
        //创建一个目录
        File dir = new File(basePath);
        if (!dir.exists()){
            dir.mkdirs();
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString()+suffix;
        try {
            file.transferTo(new File(basePath+filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(filename);
    }

    @GetMapping("/download")
    /**
     * 下载图片
     */
    public void download(String name, HttpServletResponse response){
        //输入流
        try {
            FileInputStream fis = new FileInputStream(new File(basePath+name));
            //输出流
            ServletOutputStream OutputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len=fis.read(bytes))!=-1){
                OutputStream.write(bytes,0,len);
                OutputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
