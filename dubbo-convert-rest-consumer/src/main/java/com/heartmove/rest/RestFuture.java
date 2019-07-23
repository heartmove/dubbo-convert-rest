package com.heartmove.rest;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO: 增加描述
 *
 * @author user
 * @date 2019/5/20 17:10
 * @version 1.0.0
 * @copyright wonhigh.cn
 */
public class RestFuture{

	private int timeout;
	private final Lock lock = new ReentrantLock();
	private final Condition done = lock.newCondition();
	private final long start = System.currentTimeMillis();
	private volatile long sent;

	private Invocation invocation;
	private RestInvoker invoker;
	private Object object;
	private RuntimeException exception;


	public RestFuture(int timeout, Invocation invocation, RestInvoker invoker) {
		this.timeout = timeout;
		this.invocation = invocation;
		this.invoker = invoker;
	}

	public Object get() throws Exception {
		return get(Constants.DEFAULT_TIMEOUT);
	}

	public Object get(int timeoutInMillis) throws Exception {
		if (timeout <= 0) {
			timeout = Constants.DEFAULT_TIMEOUT;
		}
		if (!isDone()) {
			long start = System.currentTimeMillis();
			lock.lock();
			try {
				while (!isDone()) {
					done.await(timeout, TimeUnit.MILLISECONDS);
					if (isDone() || System.currentTimeMillis() - start > timeout) {
						break;
					}
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				lock.unlock();
			}
			if (!isDone()) {
				throw new Exception("获取结果超时");
			}
		}
		return object;
	}

	public boolean isDone() {
		boolean flag = object != null;
		if(flag && object instanceof Exception){
			Exception ex = (Exception)object;
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return flag;
	}

	public void setObject(Object object) {
		this.object = object;
	}


}


