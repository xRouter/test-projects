package com.test.projects.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

public class ZkTool {
	
	public static String createPath(ZooKeeper zk, String path, byte[] data, List<ACL> acl, CreateMode createMode) throws Exception {
		String[] list = path.split("/");
		String zkPath = "";
		String createResult = "";
		for (String str : list) {
			if(str.equals("")) continue;
			zkPath = zkPath + "/" + str;
			if (zk.exists(zkPath, false) == null) {
				createResult = zk.create(zkPath, data, acl, createMode);
			}
		}
		return createResult;
	}
	
	public static String[] getTree(ZooKeeper zk, String path) throws Exception {
		if (zk.exists(path, false) == null) {
			return new String[0];
		}
		List<String> dealList = new ArrayList<String>();
		dealList.add(path);
		int index = 0;
		while (index < dealList.size()) {
			String tempPath = dealList.get(index);
			List<String> children = zk.getChildren(tempPath, false);
			if (tempPath.equalsIgnoreCase("/") == false) {
				tempPath = tempPath + "/";
			}
			Collections.sort(children);
			for (int i = children.size() - 1; i >= 0; i--) {
				dealList.add(index + 1, tempPath + children.get(i));
			}
			index++;
		}
		return (String[]) dealList.toArray(new String[0]);
	}
}
