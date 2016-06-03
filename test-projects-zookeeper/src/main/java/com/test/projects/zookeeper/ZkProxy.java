package com.test.projects.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.projects.zookeeper.ZkWatcher.WatcherConfig;

/**
 * zookeeper客户端代理类
 * 1，zookeeper集群链接，自动重连
 * 2，zookeeper验证
 * 3，zookeeper监听器装载
 */
public class ZkProxy {

	private static final Logger logger = LoggerFactory.getLogger(ZkProxy.class);
 
	private ZooKeeper zk;
	
	private List<ACL> acl = new ArrayList<ACL>();
	
	private String zkConnectString, userName, password;
	
	private int zkSessionTimeout;
	
	private WatcherConfig[] pathWatcherConfigs;
	
	public ZkProxy(String connectString, String userName, String password, int sessionTimeout, WatcherConfig... pathWatcherConfigs){
		//
		this.zkConnectString = connectString;
		this.userName = userName;
		this.password = password;
		this.zkSessionTimeout = sessionTimeout;
		//
		this.pathWatcherConfigs = pathWatcherConfigs;
	} 
	
	public void connect() throws Exception {
		CountDownLatch connectionLatch = new CountDownLatch(1);
		createZookeeper(connectionLatch);
		connectionLatch.await();
	}
	
	public synchronized void  reConnection() throws Exception{
		if (this.zk != null) {
			this.zk.close();
			this.zk = null;
			this.connect();
		}
	}
	
	private void createZookeeper(final CountDownLatch connectionLatch) throws Exception {
		//创建zk
		zk = new ZooKeeper(this.zkConnectString, this.zkSessionTimeout, new ZkWatcher(this, connectionLatch, pathWatcherConfigs));
		//zk认证信息处理
		String authString = this.userName + ":"+ this.password;
		zk.addAuthInfo("digest", authString.getBytes());
		
		acl.clear();
		acl.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(authString))));
		acl.add(new ACL(ZooDefs.Perms.READ, Ids.ANYONE_ID_UNSAFE));
	}
	
	public String getConnectStr(){
		return this.zkConnectString;
	}

	public List<ACL> getAcl() {
		return acl;
	}
	
	public boolean isConnected() throws Exception{
		return zk != null && zk.getState() == States.CONNECTED;
	}
	
	public void close() throws InterruptedException {
		logger.info("关闭zookeeper连接");
		this.zk.close();
	}
	
	public ZooKeeper getZooKeeper() throws Exception {
		if(!this.isConnected()){
			reConnection();
		}
		return this.zk;
	}
}
 
