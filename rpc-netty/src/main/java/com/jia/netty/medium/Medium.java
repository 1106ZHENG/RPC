package com.jia.netty.medium;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;
import com.jia.netty.mdoel.Response;
import com.jia.netty.mdoel.ServerRequest;


public class Medium {

	// 存储所有bean的 beanMethod
	public static final HashMap<String,	BeanMethod> mediaMap = new HashMap<String, BeanMethod>();
	private static Medium media = null;
	
	private Medium() {}

	// 单例模式创建
	public static Medium newInstance() {
		if (media == null) {
			media = new Medium();
		}
		return media;
	}

	/*
	 * 中介的process()很重要：通过 反射 执行方法
	 */
	public Response process(ServerRequest request) {

		Response result = null;
		
		System.out.print("mediaMap的key: " + mediaMap.keySet());
		System.out.println();
		System.out.println("media.process()要处理的command: " + request.getCommand() );
		
		try {
			String command = request.getCommand(); // command是key
			BeanMethod beanMethod = mediaMap.get(command);
			if (beanMethod == null) {
				return null;
			}
			
			Object bean = beanMethod.getBean();
			Method method = beanMethod.getMethod();
			Class<?> type = method.getParameterTypes()[0]; // 先假设只有一个参数
			Object content = request.getContent();
			Object args = JSONObject.parseObject(JSONObject.toJSONString(content), type);
			
			result = (Response) method.invoke(bean, args); // bean是实例化对象，正常是bean.method()
			result.setId(request.getId()); // ClientRequest里的id自增
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	

}
