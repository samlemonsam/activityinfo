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
package org.activityinfo.test.pageobject.gxt.tree;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.activityinfo.test.pageobject.gxt.GxtTree;

import java.util.Set;

public class CheckingVisitor implements GxtTreeVisitor {
    
    private Set<String> toCheck = Sets.newHashSet();

    public CheckingVisitor(Iterable<String> toCheck) {
        this.toCheck = Sets.newHashSet(toCheck);
    }

    @Override
    public Action visit(GxtTree.GxtNode node) {
        if(node.isLeaf()) {
            String label = node.getLabel();
            node.setChecked(toCheck.contains(label));
            toCheck.remove(label);

        } else {
            node.ensureExpanded();
            
            // There can be a delay between the initial rendering of the tree nodes
            // and updating the checked state. 
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return Action.CONTINUE;
    }

    public void validate() {
        if(!toCheck.isEmpty()) {
            throw new AssertionError("Not all nodes to check could be found. Missing: " +
                    Joiner.on(", ").join(toCheck));
        }
    }
}
