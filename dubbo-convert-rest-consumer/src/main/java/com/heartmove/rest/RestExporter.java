package com.heartmove.rest;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.protocol.AbstractExporter;

public class RestExporter<T> extends AbstractExporter<T> {

    public RestExporter(Invoker<T> invoker) {
        super(invoker);
    }

    @Override
    public Invoker<T> getInvoker() {
        return super.getInvoker();
    }

    @Override
    public void unexport() {
        super.unexport();
    }
}
