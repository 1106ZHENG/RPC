package com.jia.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.jia.netty.future.ResultFuture;
import com.jia.netty.handler.SimpleClientHandler;
import com.jia.netty.mdoel.ClientRequest;
import com.jia.netty.mdoel.Response;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyClient {

	private static ChannelFuture f = null;

	static {
		String host = "localhost";
		int port = 8081;

		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(
									new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
							ch.pipeline().addLast(new StringDecoder()); //字符串解码器
							ch.pipeline().addLast(new SimpleClientHandler()); //业务逻辑处理处
							ch.pipeline().addLast(new StringEncoder()); //字符串编码器
						}
					});

			// Start the client
			f = b.connect(host, port).sync();

		} catch (Exception e) {
			e.printStackTrace();
		}
		 // 	这里不能用shutdown关闭！！！！
	}
	
	public static Response send(ClientRequest request) {
		f.channel().writeAndFlush(JSONObject.toJSONString(request)); // 发送request
		f.channel().writeAndFlush("\r\n");
		
		ResultFuture future = new ResultFuture(request); // 异步获取request的返回结果
		return future.get();
	}

}
