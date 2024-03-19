package com.jia.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.jia.netty.future.ResultFuture;
import com.jia.netty.mdoel.Response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SimpleClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		// 如果收到心跳包
		if ("ping".equals(msg.toString())) {
			System.out.println("收到server端的心跳消息ping（读写空闲），回送心跳消息pong");
			ctx.channel().writeAndFlush("pong\r\n");
			return;
		}
		
		// 收到server的回应，设置response到ResultFuture
		Response response = JSONObject.parseObject(msg.toString(), Response.class);
		ResultFuture.receive(response);
		
//		ctx.channel().attr(AttributeKey.valueOf("msg1")).set(msg);
//		ctx.channel().close();
		
	}


}
