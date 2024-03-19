package com.jia.consumer.zk;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import com.jia.consumer.core.ChannelManager;
import com.jia.consumer.core.NettyClient;

import io.netty.channel.ChannelFuture;

public class ServerWatcher implements CuratorWatcher{

	/*
	 * 当监听到指定路径（“/netty”）下 子节点的变化时(如节点创建、删除、数据变更等，即服务器变化)，
	 * process() 方法被调用。process() 方法是 Watcher 接口的回调方法，用于处理监听到的事件
	 */
	@Override
	public void process(WatchedEvent event) throws Exception {
		
		System.out.println("ServerWatcher Process --------------------------");
		CuratorFramework client = ZooKeeperFactory.createClient();
		String path = event.getPath(); // 获取事件对象 event 中的发生变化的节点路径 path，即 Constants.SERVER_PATH
		client.getChildren().usingWatcher(this).forPath(path); // 重新设置了监听器，以便继续监听该路径的其他子节点变化。
		
		List<String> newServerPaths = client.getChildren().forPath(path); // 获得当前节点路径的 子节点【发生变化之后的服务器地址】
		System.out.println(newServerPaths);
		
		ChannelManager.realServerPath.clear(); // 清空重置
		for (String newServer : newServerPaths) {
			String[] str = newServer.split("#");
			ChannelManager.realServerPath.add(str[0] + "#" + str[1]);
		}
		
		ChannelManager.clearChannels();
		for (String realServer : ChannelManager.realServerPath) {
			String[] str = realServer.split("#");
			ChannelFuture cFuture = NettyClient.b.connect(str[0], Integer.valueOf(str[1]));
			ChannelManager.addChannel(cFuture);
		}
		
	}

}


/*
 * 假设你的 ZooKeeper 中有一个节点 /netty，包含子节点， /netty/server1、/netty/server2 等等。
 * 当有新的服务器加入或者现有服务器下线时，子节点列表会发生变化。
 * 有一个事件监听器（Watcher）在 /netty 节点上注册了监听器，当有子节点发生变化时，这个监听器会收到通知。
 * 
 * 假设有一个新的服务器加入，创建了一个新的子节点 /netty/server3，此时监听器会收到一个事件，
 * event.getPath() 方法将返回 /netty/server3，这就是发生变化的节点路径。
 */