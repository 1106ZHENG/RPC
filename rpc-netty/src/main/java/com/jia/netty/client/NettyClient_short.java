package com.jia.netty.client;

/*
 * 短链接 实现和server的简单通讯时使用
 */

import com.jia.netty.handler.SimpleClientHandler;

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
import io.netty.util.AttributeKey;

public class NettyClient_short {

	public static void main(String[] args) {
		String host = "localhost";
		int port = 8081;
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup)
			  .channel(NioSocketChannel.class)
			  .option(ChannelOption.SO_KEEPALIVE, true)
			  .handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
					ch.pipeline().addLast(new StringDecoder());
					ch.pipeline().addLast(new SimpleClientHandler());
					ch.pipeline().addLast(new StringEncoder());
				}
			});

			// Start the client
			ChannelFuture f = b.connect(host, port).sync();
			// 發送數據，異步等待返回結果
			f.channel().writeAndFlush("hello server");
			f.channel().writeAndFlush("\r\n");

			// Wait until the connection is closed by client [channelRead()].
			f.channel().closeFuture().sync();
			// 讀取通道裏的數據 來獲取返server回結果
			Object result = f.channel().attr(AttributeKey.valueOf("msg1")).get();
			System.out.println("獲取到服務端的返回結果："+result.toString());
			// 通道关闭才能
			
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}
	
}
