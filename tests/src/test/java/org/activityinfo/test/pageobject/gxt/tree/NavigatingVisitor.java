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
import org.activityinfo.test.pageobject.gxt.GxtTree;

import java.util.Iterator;
import java.util.List;


public class NavigatingVisitor implements GxtTreeVisitor {

    private final List<String> steps;
    private final Iterator<String> path;
    private String current;
    private GxtTree.GxtNode match;

    public NavigatingVisitor(List<String> steps) {
        this.steps = steps;
        this.path = steps.iterator();
        this.current = path.next();
    }

    @Override
    public Action visit(GxtTree.GxtNode node) {
        System.out.println(node.getLabel());
        if(current.equals(node.getLabel())) {
            if(!path.hasNext()) {
                match = node;
                return Action.ABORT;
            } 
            current = path.next();
            node.ensureExpanded();
        }
        return Action.CONTINUE;
    }
    
    public GxtTree.GxtNode get() {
        if(match == null) {
            throw new AssertionError("Could not find node " + Joiner.on(" / ").join(steps));
        }
        return match;
    }
}
