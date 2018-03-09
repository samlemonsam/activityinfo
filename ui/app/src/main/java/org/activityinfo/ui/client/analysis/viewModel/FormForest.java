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
package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;

import java.util.*;

/**
 * Set of selected forms
 */
public class FormForest {

    private final Map<ResourceId, FormTree> trees = new HashMap<>();

    private FormForest() {
    }

    public FormForest(FormTree tree) {
        assert tree != null;
        trees.put(tree.getRootFormId(), tree);
    }

    public FormForest(List<FormTree> trees) {
        for (FormTree tree : trees) {
            assert tree != null;
            this.trees.put(tree.getRootFormId(), tree);
        }
    }

    public static FormForest merge(List<FormForest> forests) {
        FormForest merged = new FormForest();
        for (FormForest forest : forests) {
            merged.trees.putAll(forest.trees);
        }
        return merged;
    }

    public int size() {
        return trees.size();
    }

    public List<FormField> getCommonFields() {
        if(trees.isEmpty()) {
            return Collections.emptyList();
        } else {
            return getFirst().getFields();
        }
    }

    public List<FormTree.Node> getRootNodes() {
        List<FormTree.Node> nodes = new ArrayList<>();
        for (FormTree formTree : trees.values()) {
            nodes.addAll(formTree.getRootFields());
        }
        return nodes;
    }

    public boolean isEmpty() {
        return trees.isEmpty();
    }

    public FormTree getFirstTree() {
        return trees.values().iterator().next();
    }


    public FormClass getFirst() {
        return trees.values().iterator().next().getRootFormClass();
    }

    public List<FormClass> getReferencedForms() {
        Set<ResourceId> visited = new HashSet<>();
        List<FormClass> list = new ArrayList<>();

        // Depth-first search for natural ordering
        for (FormTree formTree : trees.values()) {
            collectReferenced(formTree, formTree.getRootFields(), visited, list);
        }

        return list;
    }

    private void collectReferenced(FormTree tree, Iterable<FormTree.Node> parentFields, Set<ResourceId> visited, List<FormClass> list) {
        for (FormTree.Node parentField : parentFields) {
            if(parentField.getType() instanceof ReferenceType) {
                for (ResourceId formId : parentField.getRange()) {
                    if(!visited.contains(formId)) {
                        visited.add(formId);
                        // depth first...
                        collectReferenced(tree, parentField.getChildren(formId), visited, list);
                        // now ourselves
                        list.add(tree.getFormClass(formId));
                    }
                }
            }
        }
    }

    public FormTree findTree(ResourceId formId) {
        FormTree tree = trees.get(formId);
        if(tree == null) {
            throw new IllegalStateException("No such tree: " + tree);
        }
        return tree;
    }
}
