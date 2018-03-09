/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.geoadmin.model;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

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
