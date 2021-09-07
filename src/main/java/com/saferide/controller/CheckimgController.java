package com.saferide.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saferide.Detection;
import com.saferide.common.lang.Result;
import com.saferide.entity.Checkimg;
import com.saferide.service.CheckimgService;
import com.saferide.util.WebUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author saferide
 * @since 2021-04-23
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/checkimg")
public class CheckimgController {

    @Autowired
    private final CheckimgService checkimgService;

//    @RequiresAuthentication
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1", required = false) Integer currentPage) {
        Long userId = WebUtils.getUserId();
        if (null != userId) {
            Page page = new Page(currentPage, 100);
            IPage pageData = checkimgService.page(page, new LambdaQueryWrapper<Checkimg>()
                    .eq(Checkimg::getUserid, userId)
                    .orderByAsc(Checkimg::getId));
            return Result.success(pageData);
        } else {
            return Result.fail();
        }
    }

//    @RequiresAuthentication
    @PostMapping("/detect/{image}")
    public Result detect(@PathVariable("image") String image, @RequestBody Checkimg data) throws Exception {

        log.info(data.toString());
        Long userId = WebUtils.getUserId();
        int resultNum = Detection.detect(image);
        if (resultNum != 0) {
            data.setResult("不合格");
        } else {
            data.setResult("合格");
        }
        data.setImg(image);
        data.setUserid(userId);
        data.setCreated(LocalDateTime.now());

        checkimgService.save(data);
        return Result.success();
    }

//    @RequiresAuthentication
    @DeleteMapping("/delete/{id:\\d+}")
    public Result delete(@PathVariable("id") Long id) {

        //用户userid
        Long user_id = WebUtils.getUserId();

        //图片userid
        Checkimg data = checkimgService.getById(id);

        if (null != data && user_id.equals(data.getUserid())) {
            checkimgService.removeById(id);
            return Result.success(id);
        } else {
            return Result.fail();
        }
    }

}
