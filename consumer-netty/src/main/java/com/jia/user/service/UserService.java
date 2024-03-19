package com.jia.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jia.user.bean.User;

@Service
public class UserService {

	public void save(User user) {
		// 访问MySQL;
		System.out.println("执行UserService的save()");
	}

	public void saveList(List<User> users) {
		// TODO Auto-generated method stub
	}

}
