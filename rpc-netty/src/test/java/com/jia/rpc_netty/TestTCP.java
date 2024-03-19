package com.jia.rpc_netty;

import org.junit.Test;

import com.jia.netty.client.NettyClient;
import com.jia.netty.mdoel.ClientRequest;
import com.jia.netty.mdoel.Response;
import com.jia.netty.mdoel.User;

public class TestTCP {

	@Test
	public void testGetResponse() {
		ClientRequest request = new ClientRequest();
		request.setContent("测试长连接request: ");
		Response response = NettyClient.send(request);
		System.out.println("收到server响应： " + response.getResult());
	}

	@Test
	public void testSaveUser() {
		User user = new User();
		user.setId(1);
		user.setName("zhangsan");
		
		ClientRequest request = new ClientRequest();
		request.setCommand("com.jia.user.controller.UserController.saveUser");
		request.setContent(user);
		
		Response resp = NettyClient.send(request);
		System.out.println(resp.getResult());
	}
}
