package com.saferide.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.saferide.common.dto.LoginDto;
import com.saferide.common.lang.Result;
import com.saferide.entity.User;
import com.saferide.service.UserService;
import com.saferide.util.JwtUtils;
import com.saferide.util.WebUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController
public class AccountController {

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {

        User user = userService.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername()));
        Assert.notNull(user, "用户不存在");

        if (!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            return Result.fail("密码不正确");
        }
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(WebUtils.JWT_USER_ID_KEY, user.getId());
        String jwt = jwtUtils.generateToken(map);

        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        return Result.success(MapUtil.builder()
                .put("token", jwt)
                .put("id", user.getId())
                .put("username", user.getUsername())
                .map()
        );
    }

    @RequiresAuthentication
    @GetMapping("/info")
    public Result info() {
        Long userId = WebUtils.getUserId();
        User user = userService.getById(userId);
        if(null == user)
            return Result.fail();
        user.setPassword("");
        return Result.success(user);
    }

    @RequiresAuthentication
    @PostMapping("/logout")
    public Result logout() {
        SecurityUtils.getSubject().logout();
        return Result.success(null);
    }

}
