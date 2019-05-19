package com.heartmove.proxy;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.heartmove.rest.RestInvoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RestInvocationHandler implements InvocationHandler {

    private final Invoker<?> invoker;

    public RestInvocationHandler(Invoker<?> handler) {
        this.invoker = new RestInvoker<>(handler.getClass(), handler.getUrl());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoker.invoke(new RpcInvocation(method, args)).recreate();
    }
}
