package com.heartmove.service;

import com.heartmove.api.UserApi;
import com.heartmove.entity.User;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl implements UserApi {

    @Override
    public User add(User user) {
        user.setAge(10);
        return user;
    }
}
