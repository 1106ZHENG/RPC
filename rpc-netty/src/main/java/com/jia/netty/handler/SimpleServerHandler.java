package com.jia.netty.handler;

/*
 *  netty实战简单服务器 通信，不包括在完整项目
 */

import com.alibaba.fastjson.JSONObject;
import com.jia.netty.mdoel.Response;
import com.jia.netty.mdoel.ServerRequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class SimpleServerHandler extends ChannelInboundHandlerAdapter {

	// 读取数据
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		System.out.println("Server收到Client请求： " + msg.toString());
		ServerRequest serverRequest = JSONObject.parseObject(msg.toString(), ServerRequest.class);
		
		Response resp = new Response();
		resp.setId(serverRequest.getId());
		resp.setResult("Server is okay");
		ctx.channel().writeAndFlush(JSONObject.toJSONString(resp));
		ctx.channel().writeAndFlush(" \r\n");
//		ctx.channel().close(); // 传完必须关闭通道，client才能显示出来？ 不能關閉！！
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;

			if (event.state().equals(IdleState.READER_IDLE)) {
				System.out.println("Channel 读空闲");
				ctx.channel().close(); // 发生读空闲，关闭通道
			}
			if (event.state().equals(IdleState.WRITER_IDLE)) {
				System.out.println("Channel 写空闲");
			}

			if (event.state().equals(IdleState.ALL_IDLE)) {
				System.out.println("Channel 读写空闲");
				ctx.channel().writeAndFlush("ping\r\n");
			}
			
		}
	}
}
