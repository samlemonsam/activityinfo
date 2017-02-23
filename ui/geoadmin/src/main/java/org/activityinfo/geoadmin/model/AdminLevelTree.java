package org.activityinfo.geoadmin.model;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class AdminLevelTree {

	private AdminLevelNode root;
	private Map<Integer, AdminLevelNode> nodes;
	
	public AdminLevelTree(List<AdminLevel> levels) {
		root = new AdminLevelNode();
		nodes = Maps.newHashMap();
		
		for(AdminLevel level : levels) {
			AdminLevelNode node = new AdminLevelNode();
			node.id = level.getId();
			node.name = level.getName();
			nodes.put(level.getId(), node);
			if(level.isRoot()) {
				root.childLevels.add(node);
			}
		}
		for(AdminLevel level : levels) {
			if(!level.isRoot()) {
				AdminLevelNode parentNode = nodes.get(level.getParentId());
				AdminLevelNode childNode = nodes.get(level.getId());
				childNode.parent = parentNode;
				parentNode.childLevels.add(childNode);
			}
		}
	}
	
	public AdminLevelNode getRootNode() {
		return root;
	}
	
	public AdminLevelNode getLevelById(int id) {
		return nodes.get(id);
	}
}
