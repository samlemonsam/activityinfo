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
package org.activityinfo.ui.client.measureDialog.view;

import com.google.common.base.Optional;
import com.sencha.gxt.data.shared.TreeStore;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.subform.SubFormReferenceType;

import java.util.HashSet;
import java.util.Set;

/**
 * Fills a TreeView's store with fields that can be selected from the FormTree.
 *
 * <p>To keep the presentation simple, the </p>
 */
public class FieldTreeBuilder {

    private final FormTree formTree;
    private final TreeStore<MeasureTreeNode> store;

    private final Set<ResourceId> visitedForms = new HashSet<>();

    public FieldTreeBuilder(FormTree tree, TreeStore<MeasureTreeNode> store) {
        this.formTree = tree;
        this.store = store;
    }

    public void build(FormTree tree) {
        addReferencedForms(tree, tree.getRootFields());
        addCount(tree);
        addId(tree);
        addFields(Optional.absent(), tree.getRootFormClass());
    }

    private void addReferencedForms(FormTree tree, Iterable<FormTree.Node> nodes) {
        for (FormTree.Node childNode : nodes) {
            if(childNode.isReference() && !childNode.isSubForm()) {
                addReferencedForms(tree, childNode.getChildren());
                for (ResourceId referencedFormId : childNode.getRange()) {
                    FormClass referencedForm = tree.getFormClass(referencedFormId);
                    addReferencedForm(referencedForm);
                }
            }

        }
    }

    private void addReferencedForm(FormClass referencedForm) {
        if (!visitedForms.contains(referencedForm.getId())) {
            FormNode formNode = new FormNode(referencedForm);
            store.add(formNode);

            CountDistinctNode countNode = new CountDistinctNode(formTree.getRootFormId(), referencedForm);
            store.add(formNode, countNode);

            addFields(Optional.of(formNode), referencedForm);

            visitedForms.add(referencedForm.getId());
        }
    }

    private void addId(FormTree tree) {
        store.add(new IdNode(tree.getRootFormClass()));
    }

    private void addCount(FormTree tree) {
        store.add(new CountNode(tree.getRootFormClass()));
    }

    private void addFields(Optional<MeasureTreeNode> parentNode, FormClass formClass) {
        for (FormField field : formClass.getFields()) {
            if (isSimpleField(field)) {
                addNode(parentNode, new QuantityNode(formTree.getRootFormId(), formClass.getId(), field));
            }
        }
    }

    private boolean isSimpleField(FormField field) {
        return !(field.getType() instanceof ReferenceType) &&
                !(field.getType() instanceof SubFormReferenceType);
    }

    private void addNode(Optional<MeasureTreeNode> parentNode, MeasureTreeNode treeNode) {
        if(parentNode.isPresent()) {
            store.add(parentNode.get(), treeNode);
        } else {
            store.add(treeNode);
        }
    }
}
