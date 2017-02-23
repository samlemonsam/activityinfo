package org.activityinfo.geoadmin.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AdminLevelNode {
	int id;
	String name;
	AdminLevelNode parent;
	List<AdminLevelNode> childLevels = Lists.newArrayList();
	
	AdminLevelNode() {	
	}
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public AdminLevelNode getParent() {
		return parent;
	}
	public List<AdminLevelNode> getChildLevels() {
		return childLevels;
	}
	
	public List<AdminLevelNode> getLeafLevels() {
		if(childLevels.isEmpty()) {
			return Collections.singletonList(this);
		} else {
			List<AdminLevelNode> leaves = Lists.newArrayList();
			for(AdminLevelNode childNode : childLevels) {
				leaves.addAll(childNode.getLeafLevels());
			}
			return leaves;
		}		
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdminLevelNode other = (AdminLevelNode) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
}
