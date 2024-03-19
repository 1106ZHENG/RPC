package com.jia.user.remote;

import java.util.List;

import com.jia.consumer.annotation.Remote;
import com.jia.consumer.param.Response;
import com.jia.user.bean.User;

@Remote
public interface UserRemote {
	
	public Response saveUser(User user);
	public Response saveUserList(List<User> users);
	
}
