package com.jia.user.control;

/*
 * 被替换成 UserRemoteImpl
 */

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;

import com.jia.netty.mdoel.Response;
import com.jia.netty.mdoel.User;
import com.jia.netty.util.ResponseUtil;
import com.jia.user.service.UserService;

@Controller
public class UserController {
	
	@Resource
	private UserService service;
	
	public Response saveUser(User user){
		service.save(user);
		Response response = ResponseUtil.createSuccessResult(user);
		
		return response;
	}
	
	public Response saveUsers(List<User> userlist){
		service.saveList(userlist);
		Response response = ResponseUtil.createSuccessResult(userlist);
		
		return response;
	}
}