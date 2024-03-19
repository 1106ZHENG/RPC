package com.jia.consumer.core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelFuture;

public class ChannelManager {
	public static CopyOnWriteArrayList<ChannelFuture>  channelFutures = new CopyOnWriteArrayList<ChannelFuture>(); // 存的是client-server channels
	public static  CopyOnWriteArrayList<String> realServerPath=new CopyOnWriteArrayList<String>(); // 存的是string（"ip#port"）
	public static AtomicInteger  position = new AtomicInteger(0);//先采用轮询的方式使用send
	
	public static void addChannel(ChannelFuture cf) {
		channelFutures.add(cf);
	}

	public static void clearChannels() {
		channelFutures.clear();
	}

	public static ChannelFuture get() {
		ChannelFuture cFuture = null;
		// 1. 普通轮询
		if (position.get() >= channelFutures.size()) { // position.get() - 返回int
			position.set(0);
		} 
		cFuture = channelFutures.get(position.getAndIncrement());
		
		// 2. 加权轮询 - server的weight是几就在channelFutures加入几次和该server的连接
		
		// 选择了channel返回前判断链路是否有效 
		if (!cFuture.channel().isActive()) {
			channelFutures.remove(cFuture);
			return get();
		}
		
		return cFuture;
	}
	
}
