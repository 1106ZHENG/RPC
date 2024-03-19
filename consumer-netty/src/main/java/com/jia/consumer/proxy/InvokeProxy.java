package com.jia.consumer.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import com.jia.consumer.annotation.RemoteInvoke;
import com.jia.consumer.core.NettyClient;
import com.jia.consumer.param.ClientRequest;
import com.jia.consumer.param.Response;

/*
 * Spring Bean后置处理器:在初始化所有的bean之后被调用,
 * 用来动态地创建代理对象,并将代理对象注入到带有'@RemoteInvoke'注解的字段中.
 * 因此:使用带有 @RemoteInvoke 注解的字段时，实际上是在调用代理对象的方法，代理对象会根据拦截器中的逻辑进行处理。
 */
@Component
public class InvokeProxy implements BeanPostProcessor {

	/*
	 * postProcessBeforeInitialization在每个 bean 初始化之前被调用
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("[InvokeProxy]初始化的bean:" + bean.getClass().getName());
		Field[] fields = bean.getClass().getDeclaredFields(); // 反射获取 bean 类的所有字段
		
		for (Field field : fields) {
			if (field.isAnnotationPresent(RemoteInvoke.class))	{
				// 1. 修改属性可见性
				field.setAccessible(true); // 将字段设置为可访问，以便可以修改私有字段的值。
				
				final HashMap<Method, Class> methodMap = new HashMap<Method, Class>() ; //???why
				putMethodClass(methodMap, field);
				/*
				 * 2. 创建一个Enhancer对象，用来创建动态代理对象
				 * 	- 对哪些接口进行动态代理?
				 * 				设置代理对象实现的接口，这里使用了字段的类型作为接口.
				 * 	- 设置callback,对哪写方法进行拦截处理? 
				 * 				设置代理对象的回调函数，也就是定义了代理对象的拦截逻辑。
				 * 				使用 CGLIB 的 MethodInterceptor 接口实现了一个拦截器，拦截了目标方法的调用，并在方法调用前后执行一些逻辑。
				 * 	- 需要用netty client去调用服务端执行 
				 */
				Enhancer enhancer = new Enhancer();
				enhancer.setInterfaces(new Class[]{field.getType()});
				enhancer.setCallback(new MethodInterceptor() {
					
					@Override
					public Object intercept(Object instance, Method method, Object[] args, MethodProxy proxy) throws Throwable {
						ClientRequest clientRequest = new ClientRequest();
						clientRequest.setContent(args[0]);
						String command = methodMap.get(method).getName() + "." + method.getName();
//						String command = method.getName();
						System.out.println("InvokeProxy拦截的command是: " + command);
						clientRequest.setCommand(command);
						
						Response response = NettyClient.send(clientRequest); // 用Netty客户端调服务器 获取结果 --》完成动态代理
						System.out.println("拦截函数中获得response: "+response); // com.jia.user.remote.UserRemote.saveUser
						return response;
					}
				});
				try {
					field.set(bean, enhancer.create()); // 将创建的动态代理对象设置给 bean 的字段(注入)
				} catch (Exception e) {
						e.printStackTrace();
				}
			}
		}	
		// 2. 创建动态代理并且设置属性
		return bean;
	}

	private void putMethodClass(HashMap<Method, Class> methodMap, Field field) {

		Method[] methdos = field.getType().getDeclaredMethods();
		for (Method method : methdos) {
			methodMap.put(method, field.getType());
		}
		
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		// TODO Auto-generated method stub
		return bean;
	}

}
