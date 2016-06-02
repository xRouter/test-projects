package com.test.projects.zookeeper;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

public class ConnectedWatcher implements Watcher {  
	   
    private CountDownLatch connectedLatch = new CountDownLatch(1);  

    public ConnectedWatcher() {  
           
    }  

    @Override  
    public void process(WatchedEvent event) {  
       if (event.getState() == KeeperState.SyncConnected) {  
           connectedLatch.countDown();  
       }  
    }  
} 