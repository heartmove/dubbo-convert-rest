package com.heartmove.rest;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcResult;

import java.util.concurrent.Callable;

/**
 * TODO: 增加描述
 *
 * @author user
 * @date 2019/5/20 17:57
 * @version 1.0.0
 * @copyright wonhigh.cn
 */
public class HttpCallable implements Callable<Object> {
	private RestInvoker invoker;
	private Invocation invocation;

	@Override
	public Object call() throws Exception {
		RpcResult rpcResult = invoker.call(invocation);
		if(rpcResult.hasException()){
			Throwable throwable = rpcResult.getException();
			throw new Exception(throwable.getMessage(), throwable);
		}
		return rpcResult.getValue();
	}

	public HttpCallable(RestInvoker invoker, Invocation invocation) {
		this.invoker = invoker;
		this.invocation = invocation;
	}


}
