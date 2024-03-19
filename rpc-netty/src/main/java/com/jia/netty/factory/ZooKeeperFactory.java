package com.jia.netty.factory;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZooKeeperFactory {
	
	public static CuratorFramework client;
	
	public static CuratorFramework createClient() {
		if (client == null) {
			/*
			 *  重试机制:每隔1s试着连接一次直到连接上zookeeper服务器(port:2128)，
			 *  总共尝试3次，若都失败证明服务器可能宕机。
			 *  https://curator.apache.org/docs/getting-started
			 */
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
			client.start();
		}
		return client;
	}

	public static void main(String[] args) {
		String s = "try111";
		
		/*
		 *  Create a connection to a ZooKeeper cluster using default values
		 *  创建一个 CuratorFramework 实例，用于与 ZooKeeper 服务器建立连接。
		 */
		CuratorFramework client = ZooKeeperFactory.createClient();
		
		/*
		 * Once you have a CuratorFramework instance, you can make direct calls to ZooKeeper 
		 *	向 ZooKeeper 服务器发送数据：在创建了 CuratorFramework 实例后，通过该实例调用 create() 方法，
		 * 							创建了一个节点 /netty1，并将字符串 try111 作为节点的数据存储在 ZooKeeper 中。
		 */
		if (client != null) {
			try {
				client.create().forPath("/netty2", s.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
