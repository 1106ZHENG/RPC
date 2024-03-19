package com.jia.consumer.core;

//import java.util.HashSet;
import java.util.List;
//import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;

import com.alibaba.fastjson.JSONObject;
import com.jia.consumer.constants.Constants;
import com.jia.consumer.handler.SimpleClientHandler;
import com.jia.consumer.param.ClientRequest;
import com.jia.consumer.param.Response;
import com.jia.consumer.zk.ServerWatcher;
import com.jia.consumer.zk.ZooKeeperFactory;

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

	public static final Bootstrap b = new Bootstrap();
	private static ChannelFuture f = null;
//	public static Set<String> realServerPath = new HashSet<String>(); //用set可以去重去序号

	static {
		String host = "localhost";
		int port = 8081;

		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			b.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(
									new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
							ch.pipeline().addLast(new StringDecoder());
							ch.pipeline().addLast(new SimpleClientHandler());
							ch.pipeline().addLast(new StringEncoder());
						}
					});

			// client端获取所有zookeeper节点Constants.SERVER_PATH的子节点（即服务端的地址）
			CuratorFramework client = ZooKeeperFactory.createClient();
			List<String> serverPath = client.getChildren().forPath(Constants.SERVER_PATH); // 可能有多个子节点

			/*
			 * client端加上ZK監聽 zookeeper里节点（Constants.SERVER_PATH）的子节点变化
			 * 例如：某个子节点服务器可能宕掉了，或者在节点里加入新的子节点（新服务器加入），自动调用Watcher的process()处理
			 */
			CuratorWatcher watcher = new ServerWatcher();
			client.getChildren().usingWatcher(watcher).forPath(Constants.SERVER_PATH);

			for (String path : serverPath) {
				String[] str = path.split("#");
				ChannelManager.realServerPath.add(str[0] + "#" + str[1]); // serverPath里的子节点服务器可能上线/宕机，需要看处理过的真实服务器有哪些
				ChannelFuture cf = NettyClient.b.connect(str[0], Integer.valueOf(str[1]));
				ChannelManager.addChannel(cf);
			}

			if (ChannelManager.realServerPath.size() > 0) {
				String[] netMessageArray = ChannelManager.realServerPath.toArray()[0].toString().split("#");
				host = netMessageArray[0];
				port = Integer.valueOf(netMessageArray[1]);
			}

//			// 先只获取第一个子节点-服务器地址
//			if (realServerPath.size() > 0) { 
//				host = realServerPath.toArray()[0].toString();
//			}
//			
//			// Start the client，需要管理链接（拿到服务器列表后要管理）
//			f = b.connect(host, port).sync();

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 这里不能用shutdown关闭！！！！
	}

	public static Response send(ClientRequest request) {
		// 先轮询选择一个channel来发送request
		f = ChannelManager.get();
		// 发送
		f.channel().writeAndFlush(JSONObject.toJSONString(request)+"\r\n"); // 发送request
//		f.channel().writeAndFlush("\r\n");
		// 等待请求结果
		Long timeOut = 60l;
		ResultFuture future = new ResultFuture(request); // 异步获取request的返回结果
		return future.get(timeOut);
//		return future.get();
	}

}
