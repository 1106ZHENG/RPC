package com.jia.rpc_netty;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jia.netty.annotation.RemoteInvoke;
import com.jia.netty.mdoel.Response;
import com.jia.netty.mdoel.User;
import com.jia.user.remote.UserRemote;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRemoteInvoke.class)
@ComponentScan("com.jia")
public class TestRemoteInvoke {
	
	@RemoteInvoke
	UserRemote userRemote;
	
	@Test
	public void testSaveUser() {
		User user = new User();
		user.setId(1);
		user.setName("zhangsan");

		Response resp = userRemote.saveUser(user);
		System.out.println(resp.getResult());
	}
	
	@Test
	public void testSaveUsers() {
		User user = new User();
		user.setId(1);
		user.setName("zhangsan");
		List<User> users = new ArrayList<User>();
		users.add(user);
		Response resp = userRemote.saveUserList(users);
		System.out.println(resp.getResult());
	}
	
}
