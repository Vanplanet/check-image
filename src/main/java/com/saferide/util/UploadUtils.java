package com.saferide.util;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


public class UploadUtils {

    /**
     * @description: 读取文件
     * @author: evildoer
     * @datetime: 2021/3/14 15:46
     */
    public static ResponseEntity show(String path, ResourceLoader resourceLoader) {
        try {
            // 由于是读取本机的文件，file是一定要加上的， path是在application配置文件中的路径
            return ResponseEntity.ok(resourceLoader.getResource(path));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * @description: 保存文件到指定路径, 返回文件保存名称
     * @author: evildoer
     * @datetime: 2021/3/14 15:46
     */
    public static String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        // 生成新的文件名
        assert fileName != null;
        fileName = UUID.randomUUID().toString().replace("-", "") + fileName.substring(fileName.lastIndexOf("."));
        path = path + "/" + fileName;
        File dest = new File(path);
        // 判断文件父目录是否存在
        while (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdir();
        }
        // 保存文件
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //return dest.getName();
        return fileName;
    }
}
