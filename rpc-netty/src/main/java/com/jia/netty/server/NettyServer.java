package com.jia.netty.server;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import com.jia.netty.constant.Constants;
import com.jia.netty.factory.ZooKeeperFactory;
import com.jia.netty.handler.SimpleServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;


public class NettyServer {

	public static void main(String[] args) {
		EventLoopGroup parentGroup = new NioEventLoopGroup(); // 监听端口的accept事件
		EventLoopGroup childGroup = new NioEventLoopGroup();  // 监听channel的read事件

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();

			bootstrap.group(parentGroup, childGroup) // 启动时只启动一个port
					.option(ChannelOption.SO_BACKLOG, 128) // 并发量很高时，允许128各channels排队
					.childOption(ChannelOption.SO_KEEPALIVE, false) // TCP/IP协议栈的心跳包，默认不开启，自己写心跳机制
					.channel(NioServerSocketChannel.class) // ServerBootstrap/服务器 绑定的channel
					.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new DelimiterBasedFrameDecoder(65535, Delimiters.lineDelimiter()[0]));
							ch.pipeline().addLast(new StringDecoder()); // 以二进制传输的，要转成string
							ch.pipeline().addLast(new IdleStateHandler(60, 45, 20, TimeUnit.SECONDS));
							ch.pipeline().addLast(new SimpleServerHandler());
							ch.pipeline().addLast(new StringEncoder());
						}
					}); // 处理链路过来的数据，读取数据或者写出去
			
			// 绑定端口，启动监听器
			ChannelFuture cf = bootstrap.bind(8081).sync(); 
			CuratorFramework client = ZooKeeperFactory.createClient();
			InetAddress netAddress = InetAddress.getLocalHost();
			
			client.create().withMode(CreateMode.EPHEMERAL).forPath(Constants.SERVER_PATH, netAddress.getHostAddress().getBytes());
			
			cf.channel().closeFuture().sync();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}

}
