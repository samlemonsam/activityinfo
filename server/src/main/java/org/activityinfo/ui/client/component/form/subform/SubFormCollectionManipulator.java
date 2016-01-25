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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.form.FormPanelStyles;
import org.activityinfo.ui.client.component.form.SimpleFormPanel;
import org.activityinfo.ui.client.component.form.event.BeforeSaveEvent;
import org.activityinfo.ui.client.component.form.event.SaveFailedEvent;
import org.activityinfo.ui.client.style.ElementStyle;
import org.activityinfo.ui.client.widget.Button;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 01/18/2016.
 */
public class SubFormCollectionManipulator {

    public static final ResourceId SORT_FIELD_ID = ResourceId.valueOf("sort");

    private final FormClass subForm;
    private final FormModel formModel;
    private final FlowPanel rootPanel;
    private final Button addButton;
    private final SubFormInstanceLoader loader;

    private final Map<FormModel.SubformValueKey, SimpleFormPanel> forms = Maps.newHashMap();

    public SubFormCollectionManipulator(FormClass subForm, FormModel formModel, FlowPanel rootPanel) {
        this.subForm = subForm;
        this.formModel = formModel;
        this.rootPanel = rootPanel;
        this.loader = new SubFormInstanceLoader(formModel);

        addButton = new Button(ElementStyle.LINK);
        addButton.setLabel(I18N.CONSTANTS.addAnother());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addForm(newKey(), SubFormCollectionManipulator.this.rootPanel.getWidgetIndex(addButton));
                setDeleteButtonsState();
            }
        });

        bindEvents();
    }

    private void bindEvents() {
        formModel.getEventBus().addHandler(BeforeSaveEvent.TYPE, new BeforeSaveEvent.Handler() {
            @Override
            public void handle(BeforeSaveEvent event) {
                removeEmptyInstances();
            }
        });
        formModel.getEventBus().addHandler(SaveFailedEvent.TYPE, new SaveFailedEvent.Handler() {
            @Override
            public void handle(SaveFailedEvent event) {
                putEmptyInstanceIfAbsent();
            }
        });
    }

    private void putEmptyInstanceIfAbsent() {
        for (FormModel.SubformValueKey key : forms.keySet()) {
            if (formModel.getSubFormInstances().get(key) == null) {
                formModel.getSubFormInstances().put(key, new FormInstance(ResourceId.generateId(), subForm.getId()));
            }
        }
    }

    private void removeEmptyInstances() {
        for (FormModel.SubformValueKey key : forms.keySet()) {
            if (isEmpty(formModel.getSubFormInstances().get(key))) {
                formModel.getSubFormInstances().remove(key);
            }
        }
    }

    private static boolean isEmpty(FormInstance instance) {
        return instance.isEmpty() ||
                instance.size() == 2 && instance.get(ResourceId.valueOf("sort")) != null;
    }

    public void show() {
        loader.loadCollectionInstances(subForm).then(new AsyncCallback<List<FormInstance>>() {
            @Override
            public void onFailure(Throwable caught) {
                Log.error(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(List<FormInstance> result) {
                render();
            }
        });
    }

    private List<FormModel.SubformValueKey> getSortedKeys() {
        List<FormModel.SubformValueKey> keys = Lists.newArrayList(formModel.getKeysBySubForm(subForm));

        Collections.sort(keys, new Comparator<FormModel.SubformValueKey>() {
            @Override
            public int compare(FormModel.SubformValueKey o1, FormModel.SubformValueKey o2) {
                Double d1 = o1.getInstance().getDouble(SORT_FIELD_ID);
                Double d2 = o2.getInstance().getDouble(SORT_FIELD_ID);
                if (d1 != null && d2 != null) {
                    return d1.compareTo(d2);
                }
                return 0;
            }
        });
        return keys;
    }

    private void render() {
        List<FormModel.SubformValueKey> keys = getSortedKeys();

        if (keys.isEmpty()) {
            keys.add(newKey()); // generate new key if we don't have any existing data yets
        }

        for (FormModel.SubformValueKey key : keys) {
            addForm(key);
        }

        rootPanel.add(addButton);

    }

    private FormModel.SubformValueKey newKey() {
        FormModel.SubformValueKey newKey = new FormModel.SubformValueKey(subForm, InstanceGenerator.newUnkeyedInstance(subForm.getId()));
        FormInstance instance = new FormInstance(ResourceId.generateId(), subForm.getId());

        formModel.getSubFormInstances().put(newKey, instance);
        return newKey;
    }

    private void addForm(final FormModel.SubformValueKey key) {
        addForm(key, -1);
    }

    private void addForm(final FormModel.SubformValueKey key, int panelIndex) {

        final SimpleFormPanel formPanel = new SimpleFormPanel(formModel.getLocator());
        formPanel.asWidget().addStyleName(FormPanelStyles.INSTANCE.subformPanel());

        formPanel.addDeleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                rootPanel.remove(formPanel);
                forms.remove(key);

                if (loader.isPersisted(formModel.getSubFormInstances().get(key))) { // schedule deletion only if instance is persisted
                    formModel.getPersistedInstanceToRemoveByLocator().add(formModel.getSubFormInstances().get(key).getId());
                }

                formModel.getSubFormInstances().remove(key);

                setDeleteButtonsState();
            }
        });

        formPanel.show(formModel.getSubFormInstances().get(key));

        forms.put(key, formPanel);

        if (panelIndex == -1) {
            rootPanel.add(formPanel);
        } else {
            rootPanel.insert(formPanel, panelIndex);
        }

        // set sort field
        formModel.getSubFormInstances().get(key).set(SORT_FIELD_ID, rootPanel.getWidgetIndex(formPanel));
    }

    private void setDeleteButtonsState() {
        boolean moreThanOne = forms.size() > 1;
        for (SimpleFormPanel form : forms.values()) {
            form.getDeleteButton().get().setEnabled(moreThanOne);
        }
    }

    public Map<FormModel.SubformValueKey, SimpleFormPanel> getForms() {
        return forms;
    }
}
