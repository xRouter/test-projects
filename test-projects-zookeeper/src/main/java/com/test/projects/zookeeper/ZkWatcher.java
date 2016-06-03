package com.test.projects.zookeeper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkWatcher implements Watcher{
	
	private static final Logger logger = LoggerFactory.getLogger(ZkWatcher.class);
	
	private ZkProxy zkProxy;
	
	private CountDownLatch connectionLatch;
	
	private Map<String, Watcher> watcherRouter = new ConcurrentHashMap<String, Watcher>();
	
	public static class WatcherConfig {

		private String path;

		private Watcher watcher;

		public WatcherConfig(String path, Watcher watcher) {
			this.path = path;
			this.watcher = watcher;
		}

		public String getPath() {
			return path;
		}

		public Watcher getWatcher() {
			return watcher;
		}
	}
	
	public ZkWatcher(ZkProxy zkProxy, CountDownLatch connectionLatch, WatcherConfig... pathWatcherConfigs){
		this.zkProxy = zkProxy;
		this.connectionLatch = connectionLatch;
		//
		if(pathWatcherConfigs!=null && pathWatcherConfigs.length > 0){
			for(WatcherConfig cur : pathWatcherConfigs){
				this.watcherRouter.put(cur.getPath(), cur.getWatcher());
			}
		}
	}
	
	@Override
	public void process(WatchedEvent event) {
		processWatcher(event);
		processConnected(event);
		processExpried(event);
	}
	
	/**
	 * 监听节点变更事件(NodeDataChanged、NodeCreated、NodeDeleted)
	 * @param path
	 */
	private void regNodeChangedWatcher(String path){
		try {
			zkProxy.getZooKeeper().exists(path, true);
		} catch (Exception ex) {
			logger.error("路径({})设置节点监听器失败：{}", new Object[]{path, ex.getMessage(), ex});
		}
	}
	
	/**
	 * 监听子节点变更事件(NodeChildrenChanged)
	 * @param path
	 */
	private void regNodeChildrenChangedWatcher(String path){
		try {
			zkProxy.getZooKeeper().getChildren(path, true);
		} catch (Exception ex) {
			logger.error("路径({})设置子节点监听器失败：{}", new Object[]{path, ex.getMessage(), ex});
		}
	}
	
	private void processWatcher(WatchedEvent event){
		if (event.getType() == Event.EventType.None) {
			for(String path : watcherRouter.keySet()){
				regNodeChangedWatcher(path);
				regNodeChildrenChangedWatcher(path);
			}
			return;
		}
		String path = event.getPath();
		if(path==null){
			return;
		}
		Watcher watcher = watcherRouter.get(path);
		if(watcher==null){
			logger.info("路径({})没有注册({}:{})事件监听器！", new Object[] { event.getPath(), event.getType(), event.getState() });
			return;
		}
		try {
			watcher.process(event);
		} finally {
			regNodeChangedWatcher(path);
			regNodeChildrenChangedWatcher(path);
		}
	}
	
	private void processConnected(WatchedEvent event){
		if (event.getType() == Event.EventType.None && event.getState() == KeeperState.SyncConnected) {
			logger.info("成功连接ZK:" + zkProxy.getConnectStr());
			connectionLatch.countDown();
		}
	}
	
	private void processExpried(WatchedEvent event){
		if (event.getState() == KeeperState.Expired) {
			logger.error("会话超时，等待重新建立连接ZK:" + zkProxy.getConnectStr());
			try {
				zkProxy.reConnection();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
}
