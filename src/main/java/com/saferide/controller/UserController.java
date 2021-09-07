package com.saferide.controller;


import com.saferide.common.lang.Result;
import com.saferide.entity.User;
import com.saferide.service.UserService;
import com.saferide.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author saferide
 * @since 2021-04-23
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;


    @PostMapping("/register")
    public Result register(@Validated @RequestBody User user) {
        user.setUsername(user.getUsername());
        user.setPassword(MD5Util.string2MD5(user.getPassword()));
        user.setCreated(LocalDateTime.now());

        userService.save(user);

        return Result.success(user);
    }
}
