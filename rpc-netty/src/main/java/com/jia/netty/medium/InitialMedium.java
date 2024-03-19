package com.jia.netty.medium;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.jia.netty.annotation.Remote;

@Component
public class InitialMedium implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// TODO Auto-generated method stub
		return bean;
	}

	/*
	 * 在所有的Bean初始化之后执行，
	 * 初始化/实例化完成后应该获取到所有@controller类和里面的方法【是业务逻辑】
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if (bean.getClass().isAnnotationPresent(Remote.class)) { // 原本是Controller.class，总之是实际提供业务逻辑的接口
			Method[] methods = bean.getClass().getDeclaredMethods();
			for (Method m : methods) {
				String key = bean.getClass().getInterfaces()[0].getName() + "."+m.getName(); 
//				String key = m.getName();// com.jia.netty.remote.UserRemote.saveUser
				HashMap<String, BeanMethod> map = Medium.mediaMap;
				BeanMethod beanMethod = new BeanMethod();
				beanMethod.setBean(bean);
				beanMethod.setMethod(m);
				map.put(key, beanMethod);
			}
		}
		return bean;
	}
	
}
