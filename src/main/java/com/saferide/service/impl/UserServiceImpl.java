package com.saferide.service.impl;

import com.saferide.entity.User;
import com.saferide.mapper.UserMapper;
import com.saferide.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author saferide
 * @since 2021-04-23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
