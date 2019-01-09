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
package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.view.field.*;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

import java.util.ArrayList;
import java.util.List;


/**
 * View that displays a form's fields and sub forms and accepts user input.
 */
public class FormPanel implements IsWidget {

    private final FormSource formSource;

    private final CssFloatLayoutContainer panel;

    private final List<FieldView> fieldViews = new ArrayList<>();
    private final List<RepeatingSubFormPanel> repeatingSubForms = new ArrayList<>();
    private final List<KeyedSubFormPanel> keyedSubFormPanels = new ArrayList<>();

    private InputHandler inputHandler;
    private RecordRef recordRef;
    private Maybe<RecordTree> existingRecord;

    private int horizontalPadding = 0;

    private FormInputViewModel viewModel;
    private final TextButton deleteButton;

    public FormPanel(FormSource formSource, FormTree formTree, RecordRef recordRef, InputHandler inputHandler, Maybe<RecordTree> existingRecord) {
        this.formSource = formSource;

        assert recordRef != null;

        InputResources.INSTANCE.style().ensureInjected();

        this.recordRef = recordRef;
        this.existingRecord = existingRecord;
        this.inputHandler = inputHandler;

        panel = new CssFloatLayoutContainer();
        panel.addStyleName(InputResources.INSTANCE.style().form());

        if(formTree.getRootFormClass().isSubForm()) {
            panel.addStyleName(InputResources.INSTANCE.style().subform());
        }

        FieldWidgetFactory widgetFactory = new FieldWidgetFactory(formSource, formTree, !existingRecord.isVisible());

        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.isSubForm()) {
                if(node.isSubFormVisible()) {
                    addSubForm(formTree, node);
                }
            } else if(node.isParentReference()) {
                // ignore
            } else if(node.getField().isVisible() && !isSubFormKey(node)) {
                FieldWidget fieldWidget = widgetFactory.create(node.getField(), new FieldUpdater() {
                    @Override
                    public void update(FieldInput input) {
                        onInput(node, input);
                    }

                    @Override
                    public void touch() {
                        onTouch(node);
                    }

                });

                if (fieldWidget != null) {
                    addField(node, fieldWidget);
                }
            }
        }
        deleteButton = new TextButton(I18N.CONSTANTS.remove());

        if(formTree.getRootFormClass().isSubForm()) {
            deleteButton.addSelectHandler(event -> onDelete());
            panel.add(deleteButton, new CssFloatLayoutContainer.CssFloatData(1,
                    new Margins(0, horizontalPadding, 10, horizontalPadding)));
        }
    }

    private boolean isSubFormKey(FormTree.Node node) {
        return node.getDefiningFormClass().isSubForm() && node.getField().isKey() &&
                node.getType() instanceof PeriodType;
    }

    private void onDelete() {
        MessageBox messageBox = new MessageBox(I18N.CONSTANTS.confirmDeletion());
        messageBox.setMessage(I18N.CONSTANTS.confirmDeleteRecord());
        messageBox.setPredefinedButtons(Dialog.PredefinedButton.OK, Dialog.PredefinedButton.CANCEL);
        messageBox.getButton(Dialog.PredefinedButton.OK)
                .addSelectHandler(e -> inputHandler.deleteSubRecord(this.recordRef));
        messageBox.show();
    }

    public RecordRef getRecordRef() {
        return recordRef;
    }


    public void init(FormInputViewModel viewModel) {

        this.recordRef = viewModel.getRecordRef();

        for (FieldView fieldView : fieldViews) {
            fieldView.init(viewModel);
        }

        for (RepeatingSubFormPanel subFormView : repeatingSubForms) {
            subFormView.init(viewModel.getSubForm(subFormView.getFieldId()));
        }
    }

    public void updateView(FormInputViewModel viewModel) {

        this.viewModel = viewModel;

        deleteButton.setEnabled(!viewModel.isLocked());

        // Update Field Views
        boolean layoutRequired = false;
        for (FieldView fieldView : fieldViews) {
            if(fieldView.updateView(viewModel)) {
                layoutRequired = true;
            }
        }

        // Update Subforms
        for (RepeatingSubFormPanel subFormView : repeatingSubForms) {
            subFormView.updateView(viewModel.getSubForm(subFormView.getFieldId()));
        }
        for (KeyedSubFormPanel subFormView : keyedSubFormPanels) {
            subFormView.updateView(viewModel.getSubForm(subFormView.getFieldId()));
        }

        if(layoutRequired) {
            panel.forceLayout();
        }
    }

    private void onInput(FormTree.Node node, FieldInput input) {
        if(viewModel.isPlaceholder()) {
            inputHandler.updateSubModel(viewModel.getInputModel().update(node.getFieldId(), input));

        } else {
            inputHandler.updateModel(recordRef, node.getFieldId(), input);
        }
    }

    private void onTouch(FormTree.Node node) {
        inputHandler.touchField(recordRef, node.getFieldId());
    }

    private void addField(FormTree.Node node, FieldWidget fieldWidget) {

        FieldView fieldView = new FieldView(node.getField(), fieldWidget, horizontalPadding);

        panel.add(fieldView, new CssFloatLayoutContainer.CssFloatData(1,
                new Margins(10, horizontalPadding, 10, horizontalPadding)));

        fieldViews.add(fieldView);
    }

    /**
     * Scrolls to the first field with an error, and shifts focus to it.
     */
    public void scrollToFirstError() {
        for (FieldView fieldView : fieldViews) {
            if(!fieldView.isValid()) {
                fieldView.focusTo();
                break;
            }
        }
    }

    private void addSubForm(FormTree formTree, FormTree.Node node) {
        SubFormReferenceType subFormType = (SubFormReferenceType) node.getType();
        FormTree subTree = formTree.subTree(subFormType.getClassId());
        SubFormKind subFormKind = subTree.getRootFormClass().getSubFormKind();

        if(subFormKind == SubFormKind.REPEATING) {
            RepeatingSubFormPanel subPanel = new RepeatingSubFormPanel(formSource, node, subTree, inputHandler, existingRecord);

            panel.add(subPanel, new CssFloatLayoutContainer.CssFloatData(1));
            repeatingSubForms.add(subPanel);

        } else {
            KeyedSubFormPanel subPanel = new KeyedSubFormPanel(recordRef, formSource, node, subTree, inputHandler, existingRecord);
            panel.add(subPanel, new CssFloatLayoutContainer.CssFloatData(1));
            keyedSubFormPanels.add(subPanel);
        }
    }

    public void setBorders(boolean borders) {
        panel.setBorders(borders);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

}
