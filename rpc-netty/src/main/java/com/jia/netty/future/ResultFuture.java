package com.jia.netty.future;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jia.netty.mdoel.ClientRequest;
import com.jia.netty.mdoel.Response;

public class ResultFuture {
	
	/*
	 * 一个request应该对应一个response，main thread获取work threads的结果会存在并发访问的问题
	 * 一个work thread会处理很多request，要用一个集合维护
	 */
	public final static ConcurrentHashMap<Long, ResultFuture> resultMap = new ConcurrentHashMap<Long, ResultFuture>();
	private Response response;
	final Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	
	public ResultFuture(ClientRequest request) {
		resultMap.put(request.getId(), this);
	}

	public Response get() {
		lock.lock();
		try {
			while (!respDone()) { // 防止虚假唤醒，可能是别的
				condition.await();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return this.response;
	}

	// 把处理request得到的response设置进Result Future对象里
	public static void receive(Response response) {
		if (response != null) {
			ResultFuture future = resultMap.get(response.getId());
			if (future != null) {
				Lock lock = future.lock; //?
				lock.lock();
				try {
					future.setResponse(response);
					future.condition.signal();
					resultMap.remove(future); // 只remove掉value吗？response.getId()
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
	}
	
	private boolean respDone() {
		if (this.response != null) {
			return true;
		}
		return false;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}
	
}
