package com.jia.consumer.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jia.consumer.param.ClientRequest;
import com.jia.consumer.param.Response;

public class ResultFuture {
	
	/*
	 * 一个request应该对应一个response，main thread获取work threads的结果会存在并发访问的问题
	 * 一个work thread会处理很多request，要用一个集合维护
	 */
	public final static ConcurrentHashMap<Long, ResultFuture> resultMap = new ConcurrentHashMap<Long, ResultFuture>();
	private Response response;
	final Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();

	private Long timeout = 2*60*1000l; // 默认超时时间
	private Long startTime = System.currentTimeMillis();
	
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
	
	public Response get(long time) {
		lock.lock();
		try {
			while (!respDone()) { // 防止虚假唤醒
				condition.await(time, TimeUnit.SECONDS);
				if((System.currentTimeMillis()-startTime)>time) { // 等待response超时
					System.out.println("请求超时");
					break;
				}
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
	
	public Long getTimeout() {
		return timeout;
	}

	public Long getStartTime() {
		return startTime;
	}
	
	/*
	 * 定义一个任务线程:
	 * 对所有的ResultFuture对象定时 遍历处理,把超时的移除掉
	 */
	static class FutureThread extends Thread {

		@Override
		public void run() {
			
			for (Long id : resultMap.keySet()) {
				ResultFuture rf = resultMap.get(id);
				if (rf == null) {
					resultMap.remove(rf);
				} else {
					// 如果 链路超时
					if (rf.getTimeout() > (System.currentTimeMillis() - rf.getStartTime())) {
						// 构建超时连接 response
						Response resp = new Response();
						resp.setId(id);
						resp.setCode("33333"); // 不是“00000”的任何字符串
						resp.setMsg("网络请求超时");
						receive(resp);
					}
				}
			}
		}
	}
	
}
