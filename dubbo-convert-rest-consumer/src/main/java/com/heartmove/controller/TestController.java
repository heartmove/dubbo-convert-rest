package com.heartmove.controller;

import com.heartmove.api.UserApi;
import com.heartmove.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestController {

    @Resource
    private UserApi userApi;


    @GetMapping("/save/{userName}")
    public User  save(@PathVariable String userName){
        User user = new User();
        user.setName(userName);
        return userApi.add(user);
    }
}
