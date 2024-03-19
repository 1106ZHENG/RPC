package com.jia.user.remote;

import java.util.List;

import javax.annotation.Resource;

import com.jia.netty.annotation.Remote;
import com.jia.netty.mdoel.Response;
import com.jia.netty.mdoel.User;
import com.jia.netty.util.ResponseUtil;
import com.jia.user.service.UserService;

@Remote
public class UserRemoteImpl implements UserRemote {
	
	@Resource
	UserService userService;
	
	@Override
	public Response saveUser(User user) {
		userService.save(user);
		return ResponseUtil.createSuccessResult(user);
	}
	
	@Override
	public Response saveUserList(List<User> users) {
		userService.saveList(users);
		return ResponseUtil.createSuccessResult(users);
	}
}