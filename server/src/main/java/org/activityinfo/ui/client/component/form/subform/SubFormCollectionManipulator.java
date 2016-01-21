package org.activityinfo.ui.client.component.form.subform;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.form.SimpleFormPanel;
import org.activityinfo.ui.client.style.ElementStyle;
import org.activityinfo.ui.client.widget.Button;

import java.util.Map;
import java.util.Set;

/**
 * @author yuriyz on 01/18/2016.
 */
public class SubFormCollectionManipulator {

    private final FormClass subForm;
    private final FormModel formModel;
    private final FlowPanel rootPanel;

    private final Map<FormModel.SubformValueKey, SimpleFormPanel> forms = Maps.newHashMap();

    public SubFormCollectionManipulator(FormClass subForm, FormModel formModel, FlowPanel rootPanel) {
        this.subForm = subForm;
        this.formModel = formModel;
        this.rootPanel = rootPanel;
    }

    public void show() {
        rootPanel.add(createAddButton());

        Set<FormModel.SubformValueKey> keys = Sets.newHashSet(formModel.getKeysBySubForm(subForm));
        if (keys.isEmpty()) {
            keys.add(newKey()); // generate new key if we don't have any existing data yets
        }

        for (FormModel.SubformValueKey key : keys) {
            addForm(key);
        }
    }

    private FormModel.SubformValueKey newKey() {
        FormModel.SubformValueKey newKey = new FormModel.SubformValueKey(subForm, InstanceIdGenerator.newUnkeyedInstance(subForm.getId()));
        FormInstance instance = new FormInstance(ResourceId.generateId(), subForm.getId());
        formModel.getSubFormInstances().put(newKey, instance);
        return newKey;
    }

    private Button createAddButton() {
        final Button button = new Button(ElementStyle.LINK);
        button.setLabel(I18N.CONSTANTS.addAnother());
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addForm(newKey());
                setDeleteButtonsState();
            }
        });
        return button;
    }

    private void addForm(final FormModel.SubformValueKey key) {

        final SimpleFormPanel formPanel = new SimpleFormPanel(formModel.getLocator());

        formPanel.addDeleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                rootPanel.remove(formPanel);
                forms.remove(key);
                formModel.getSubFormInstances().remove(key);

                setDeleteButtonsState();
            }
        });

        formPanel.show(formModel.getSubFormInstances().get(key));

        forms.put(key, formPanel);

        rootPanel.add(formPanel);

    }

    private void setDeleteButtonsState() {
        boolean moreThanOne = forms.size() > 1;
        for (SimpleFormPanel form : forms.values()) {
            form.getDeleteButton().get().setEnabled(moreThanOne);
        }
    }
}
