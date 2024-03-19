package com.jia.user.remote;

import java.util.List;

import com.jia.netty.annotation.Remote;
import com.jia.netty.mdoel.Response;
import com.jia.netty.mdoel.User;

@Remote
public interface UserRemote {
	
	public Response saveUser(User user);
	public Response saveUserList(List<User> users);
	
}
