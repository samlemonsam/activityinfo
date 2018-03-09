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
package org.activityinfo.model.formTree;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author yuriyz on 03/24/2015.
 */
public class TFormTree {

    private final FormTree tree;

    public TFormTree(FormTree tree) {
        Preconditions.checkNotNull(tree);
        this.tree = tree;
    }

    public List<FieldPath> getRootPaths() {
        List<FieldPath> result = Lists.newArrayList();
        for (FormTree.Node node : tree.getRootFields()) {
            result.add(node.getPath());
        }
        return result;
    }

    public FormTree getTree() {
        return tree;
    }
}
