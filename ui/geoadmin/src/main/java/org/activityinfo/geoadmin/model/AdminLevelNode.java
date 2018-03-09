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

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

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
