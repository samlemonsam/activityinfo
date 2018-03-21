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
package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.tree.Tree;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.analysis.viewModel.FormForest;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the user to choose a new dimension
 */
public class NewDimensionDialog implements HasSelectionHandlers<DimensionModel> {

    private AnalysisViewModel model;
    private Dialog dialog;
    private Subscription subscription;

    private final SimpleEventBus eventBus = new SimpleEventBus();

    private TreeStore<DimensionNode> store;
    private Tree<DimensionNode, String> tree;


    public NewDimensionDialog(AnalysisViewModel model) {
        this.model = model;

        store = new TreeStore<>(DimensionNode::getKey);
        tree = new Tree<>(store, DimensionNode.VALUE_PROVIDER);
        tree.setIconProvider(DimensionNode::getIcon);

        this.dialog = new Dialog();
        dialog.setHeading("New Dimension");
        dialog.setPixelSize(640, 480);
        dialog.setResizable(true);
        dialog.setPredefinedButtons(Dialog.PredefinedButton.CANCEL, Dialog.PredefinedButton.OK);
        dialog.setClosable(true);
        dialog.addDialogHideHandler(this::onDialogHidden);
        dialog.setWidget(tree);

        dialog.getButton(Dialog.PredefinedButton.CANCEL).addSelectHandler(this::onCancelClicked);
        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(this::onOkClicked);
    }


    public void show() {
        subscription = model.getFormForest().subscribe(this::onForestUpdated);
        dialog.show();
        dialog.center();
    }

    private void onForestUpdated(Observable<FormForest> forest) {
        store.clear();
        if (forest.isLoaded()) {

            // The Form Name can be a dimension
            store.add(new FormNode());

            // Add root fields...
            for (FormTree.Node node : forest.get().getRootNodes()) {
                if(isEligible(node.getType())) {
                    store.add(new RootFieldNode(node.getDefiningFormClass().getId(), node.getField()));
                }
            }

            // Add reference forms with eligible fields
            for (FormClass formClass : forest.get().getReferencedForms()) {
                List<DimensionNode> children = new ArrayList<>();
                for (FormField field : formClass.getFields()) {
                    if(isEligible(field.getType())) {
                        children.add(new ReferencedNode(formClass, field));
                    }
                }
                if(!children.isEmpty()) {
                    ReferenceFormNode refNode = new ReferenceFormNode(formClass, children.get(0));
                    store.add(refNode);
                    store.add(refNode, children);
                }
            }
        }
    }

    private boolean isEligible(FieldType type) {
        return type instanceof TextType ||
                type instanceof BarcodeType ||
                type instanceof EnumType ||
                type instanceof LocalDateType ||
                type instanceof PeriodType;
    }

    private void onOkClicked(SelectEvent event) {
        DimensionModel selectedItem = tree.getSelectionModel().getSelectedItem().dimensionModel();
        if (selectedItem != null) {
            SelectionEvent.fire(this, selectedItem);
            dialog.hide();
        }
    }

    private void onCancelClicked(SelectEvent event) {
        dialog.hide();
    }


    private void onDialogHidden(DialogHideEvent dialogHideEvent) {
        subscription.unsubscribe();
    }


    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<DimensionModel> selectionHandler) {
        return eventBus.addHandler(SelectionEvent.getType(), selectionHandler);
    }

    @Override
    public void fireEvent(GwtEvent<?> gwtEvent) {
        eventBus.fireEvent(gwtEvent);
    }
}
