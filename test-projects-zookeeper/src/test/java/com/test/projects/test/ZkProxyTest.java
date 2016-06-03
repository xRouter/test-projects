package com.test.projects.test;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.test.projects.zookeeper.ZkProxy;
import com.test.projects.zookeeper.ZkWatcher.WatcherConfig;

public class ZkProxyTest {

	private static final String zkConnectionString = "192.168.202.107:2181,192.168.202.107:2182,192.168.202.107:2183";
	
	public static void main(String[] args) throws Exception {
		ZkProxy zkProxy = new ZkProxy(zkConnectionString, "admin", "admin", 5000, new WatcherConfig("/mytest/a/b", new Watcher(){
			@Override
			public void process(WatchedEvent event) {
				System.out.println("自定义事件: path:"+event.getPath()+" event:"+event.getType()+" state:"+event.getState());
			}
		}));
		
		zkProxy.connect();
		
		while(true){
			Thread.sleep(100);
		}
	}
}
