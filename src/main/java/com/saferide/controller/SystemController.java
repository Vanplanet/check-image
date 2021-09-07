package com.saferide.controller;


import com.saferide.common.lang.Result;
import com.saferide.util.UploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/system")
@Slf4j
public class SystemController {

    @Value("${web.upload.path}")
    private String uploadPath;

    @Value("${web.upload.show}")
    private String showPath;

    private final ResourceLoader resourceLoader;

    public SystemController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * @description: 上传图片
     * @param file 文件
     * @param prefix 文件保存的上级文件夹
     * @author: evildoer
     * @datetime: 2021/3/14 16:11
     */

//    @RequiresAuthentication
    @PostMapping("/upload/{prefix}")
    public Result upload(@RequestParam("file") MultipartFile file, @PathVariable("prefix") String prefix) {
        String path = UploadUtils.upload(file, uploadPath + "/" + prefix);
        if (null == path) {
            return Result.fail("上传失败");
        }
//        path = showPath + "/" + path;

        return Result.success(path);
    }

}
