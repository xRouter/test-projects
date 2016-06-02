package com.test.projects.zookeeper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

public class ZooKeeperClient implements Watcher {

	private static final String zkConnectionString = "192.168.202.107:2181,192.168.202.107:2182,192.168.202.107:2183";
	
	private static final ZooKeeper zooKeeper;

	public static final ZooKeeperClient instance = new ZooKeeperClient();

	static {
		ZooKeeper tmpZk = null;
		try {
			tmpZk = new ZooKeeper(zkConnectionString, 5000, instance);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			zooKeeper = tmpZk;
		}
		instance.inti();
	}

	private CountDownLatch connectedLatch = new CountDownLatch(1);

	private Map<String, String> bizInstanceMap = new ConcurrentHashMap<String, String>();
	
	private String bizInstancePath = "/workers/instances/";

	private ZooKeeperClient() {

	}

	public void inti() {
		//避免没有连接成功就开始做zk相关操作
		if (States.CONNECTING.equals(zooKeeper.getState())) {  
            try { 
            	if(connectedLatch.getCount()!=1){
            		connectedLatch = new CountDownLatch(1);
            	}
            	System.out.println("等待连接zk:"+zkConnectionString+"");
                connectedLatch.await();  
            } catch (InterruptedException e) {  
                throw new IllegalStateException(e);  
            }  
        }  
		String bizInstancePath = "/workers/myBiz1/instances/id#";
		String curInstancePath = null;
		try {
			checkAndAutoCreateParentPath(bizInstancePath);
			curInstancePath = zooKeeper.create(bizInstancePath,	"实例创建临时列表，用于选举master".getBytes("GB2312"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		} catch (Exception ex) {
			throw new RuntimeException("创建业务实例监控列表异常:" + ex.getLocalizedMessage());
		}
		System.out.println("createPath:"+curInstancePath);
		bizInstanceMap.put(bizInstancePath, curInstancePath);
	}
	
	//common
	private void checkAndAutoCreateParentPath(String curPath)throws KeeperException, InterruptedException{
		if( curPath == null || curPath.trim().length()==0 ) return;
		String[] splits = curPath.split("/");
		for(int i = 0; i < splits.length; i++){ 
			String parentPath = StringUtils.join(Arrays.copyOfRange(splits, 0, i), "/");
			if(parentPath.trim().equals("")) continue;
			if(zooKeeper.exists(parentPath, true) == null){
				zooKeeper.create(parentPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				System.out.println("成功创建zk路径："+parentPath);
			}
		}
	}
	
//	private boolean isMaster(){
//		try {
//			zooKeeper.getChildren(bizInstancePath, this);
//		} catch (Exception ex) {
//			System.err.println(bizInstancePath + "|getChildern watcher ex:"	+ ex.getLocalizedMessage());
//		}
//	}

	@Override
	public void process(WatchedEvent event) {
		//可以进行后续操作了
		if (event.getState() == KeeperState.SyncConnected) {
			System.out.println("成功连接zk:"+zkConnectionString+"");
			connectedLatch.countDown();
		}
		System.out.println(event.getPath() + " 事件: " + event.getType().name());
		for (String bizInstancePath : bizInstanceMap.keySet()) {
			// 业务实例路径
			if (bizInstancePath.equals(event.getPath())) {
				try {
					zooKeeper.getChildren(bizInstancePath, this);
				} catch (Exception ex) {
					System.err.println(bizInstancePath + "|getChildern watcher ex:"	+ ex.getLocalizedMessage());
				}
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("zk test...");
		try {
			System.in.read();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
