package com.jia.netty.init;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;


import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.jia.netty.constant.Constants;
import com.jia.netty.factory.ZooKeeperFactory;
import com.jia.netty.handler.ServerHandler;
//import com.jia.netty.handler.SimpleServerHandler;

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

@Component
public class NettyInitial implements ApplicationListener<ContextRefreshedEvent> {

	/*
	 * Spring 应用程序启动 - ApplicationContext被初始化或者刷新时，会触发 ContextRefreshedEvent 事件,
	 * ApplicationListener接口的onApplicationEvent() 方法会被自动调用处理ContextRefreshedEvent 事件，
	 * 从而触发 start() 方法的执行。
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.start();
	}

	private void start() {
		EventLoopGroup parentGroup = new NioEventLoopGroup(); // 监听端口的accept事件
		EventLoopGroup childGroup = new NioEventLoopGroup(); // 监听channel的read事件

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();

			bootstrap.group(parentGroup, childGroup) // 启动时只启动一个port
					.option(ChannelOption.SO_BACKLOG, 128) // 设置TCP队列大小:包含已连接+未连接。并发量很高时，允许128各channels排队。
					.childOption(ChannelOption.SO_KEEPALIVE, false) // TCP/IP协议栈的心跳包，默认不开启，自己写心跳机制
					.channel(NioServerSocketChannel.class) // ServerBootstrap/服务器 绑定的channel
					.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							// 设置\r\n为分隔符
							ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
							ch.pipeline().addLast(new StringDecoder()); // 以二进制传输的，要转成string。字符串解码器
							ch.pipeline().addLast(new IdleStateHandler(60, 45, 20, TimeUnit.SECONDS));
							ch.pipeline().addLast(new ServerHandler()); // //业务逻辑处理处
							ch.pipeline().addLast(new StringEncoder()); // 字符串编码器
						}
					}); // 处理链路过来的数据，读取数据或者写出去

			// 绑定端口，启动监听器
			int port = 8081;
			ChannelFuture cf = bootstrap.bind(port).sync();
			
			CuratorFramework client = ZooKeeperFactory.createClient();
			InetAddress address = InetAddress.getLocalHost();
			if (client != null) {
				System.out.println(client);
				client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(Constants.SERVER_PATH+"/"+address.getHostAddress()+"#"+port+"#");
				System.out.println("Server注册成功");
			}
			
			cf.channel().closeFuture().sync();
			
			System.out.println("Server Closed");
		} catch (Exception e) {
			e.printStackTrace();
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}

}
