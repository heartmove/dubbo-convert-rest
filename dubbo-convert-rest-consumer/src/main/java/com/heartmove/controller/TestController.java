package com.heartmove.controller;

import com.alibaba.dubbo.rpc.RpcContext;
import com.heartmove.api.UserApi;
import com.heartmove.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.Future;

@RestController
public class TestController {

    @Resource
    private UserApi userApi;


    @GetMapping("/save/{userName}")
    public User  save(@PathVariable String userName) throws Exception{
        User user = new User();
        user.setName(userName);
        userApi.add(user);
        Future<User> future = RpcContext.getContext().getFuture();
        return future.get();
    }
}
