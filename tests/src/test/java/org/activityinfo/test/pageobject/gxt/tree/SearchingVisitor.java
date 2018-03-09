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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.activityinfo.test.pageobject.gxt.GxtTree;

public class SearchingVisitor implements GxtTreeVisitor {

    private Predicate<GxtTree.GxtNode> predicate;
    private Optional<GxtTree.GxtNode> match = Optional.absent();

    public SearchingVisitor(Predicate<GxtTree.GxtNode> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Action visit(GxtTree.GxtNode node) {
        if(predicate.apply(node)) {
            this.match = Optional.of(node);
            return Action.ABORT;
        } else {
            if(!node.isLeaf()) {
                node.ensureExpanded();
                System.out.println(node + " is expanded");
            } else {
                System.out.println(node + " is a leaf, not expanding");
            }
            return Action.CONTINUE;
        }
    }

    public Optional<GxtTree.GxtNode> getMatch() {
        return match;
    }
    
    public static SearchingVisitor byLabel(final String label) {
        return new SearchingVisitor(new Predicate<GxtTree.GxtNode>() {
            @Override
            public boolean apply(GxtTree.GxtNode input) {
                return input.getLabel().equals(label);
            }
        });
    }
}
