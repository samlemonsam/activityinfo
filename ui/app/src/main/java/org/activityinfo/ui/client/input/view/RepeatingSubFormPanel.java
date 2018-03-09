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

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.SubFormViewModel;

import java.util.HashMap;
import java.util.Map;

/**
 * View containing a list of {@link FormPanel}s, one for each sub record.
 */
public class RepeatingSubFormPanel implements IsWidget {

    private final ResourceId fieldId;
    private InputHandler inputHandler;
    private FormSource formSource;
    private final FormTree subTree;

    private final FieldSet fieldSet;
    private final CssFloatLayoutContainer container;
    private final CssFloatLayoutContainer recordContainer;
    private final Map<RecordRef, FormPanel> panelMap = new HashMap<>();

    private SubFormViewModel viewModel;

    public RepeatingSubFormPanel(FormSource formSource, FormTree.Node node, FormTree subTree, InputHandler inputHandler) {
        this.formSource = formSource;
        this.subTree = subTree;
        this.fieldId = node.getFieldId();
        this.inputHandler = inputHandler;

        recordContainer = new CssFloatLayoutContainer();

        TextButton addButton = new TextButton(I18N.CONSTANTS.addAnother());
        addButton.addSelectHandler(this::addRecordHandler);

        container = new CssFloatLayoutContainer();
        container.add(recordContainer, new CssFloatLayoutContainer.CssFloatData(1)  );
        container.add(addButton, new CssFloatLayoutContainer.CssFloatData(1, new Margins(20, 0, 5, 0)));

        fieldSet = new FieldSet();
        fieldSet.setHeading(subTree.getRootFormClass().getLabel());
        fieldSet.setCollapsible(true);
        fieldSet.setWidget(container);
        fieldSet.collapse();
    }

    @Override
    public Widget asWidget() {
        return fieldSet;
    }


    public ResourceId getFieldId() {
        return fieldId;
    }

    public void init(SubFormViewModel viewModel) {
    }

    public void updateView(SubFormViewModel viewModel) {

        this.viewModel = viewModel;

        // First add any records which are not yet present.
        for (FormInputViewModel subRecord : viewModel.getSubRecords()) {
            FormPanel subPanel = panelMap.get(subRecord.getRecordRef());
            if(subPanel == null) {
                subPanel = new FormPanel(formSource, subTree, subRecord.getRecordRef(), inputHandler);
                subPanel.init(subRecord);

                recordContainer.add(subPanel, new CssFloatLayoutContainer.CssFloatData(1));
                panelMap.put(subRecord.getRecordRef(), subPanel);
            }
            subPanel.updateView(subRecord);
        }

        // Now remove any that have been deleted
        for (FormPanel formPanel : panelMap.values()) {
            if(!viewModel.getSubRecordRefs().contains(formPanel.getRecordRef())) {
                recordContainer.remove(formPanel);
            }
        }
    }


    private void addRecordHandler(SelectEvent event) {

        // If we have a placeholder, then add it first, otherwise it will
        // disappear
        Optional<FormInputViewModel> placeholder = viewModel.getPlaceholder();
        if(placeholder.isPresent()) {
            inputHandler.addSubRecord(placeholder.get().getRecordRef());
        }

        // Add a new sub record with unique ID
        ResourceId subFormId = subTree.getRootFormId();
        ResourceId newSubRecordId = ResourceId.generateId();

        inputHandler.addSubRecord(new RecordRef(subFormId, newSubRecordId));
    }

}
