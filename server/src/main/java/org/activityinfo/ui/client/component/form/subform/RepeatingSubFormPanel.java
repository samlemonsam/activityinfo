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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.form.FormPanelStyles;
import org.activityinfo.ui.client.component.form.SimpleFormPanel;
import org.activityinfo.ui.client.component.form.event.BeforeSaveEvent;
import org.activityinfo.ui.client.style.ElementStyle;
import org.activityinfo.ui.client.widget.Button;
import org.activityinfo.ui.client.widget.LoadingPanel;

import java.util.*;

/**
 * @author yuriyz on 01/18/2016.
 */
public class RepeatingSubFormPanel implements SubFormPanel {

    private final FlowPanel panel;

    private final FormClass subForm;
    private final FormModel formModel;
    private final Button addButton;
    private final SubFormInstanceLoader loader;
    private final LoadingPanel<Void> loadingPanel;

    private final Map<FormInstance, SimpleFormPanel> forms = Maps.newHashMap();

    public RepeatingSubFormPanel(FormClass subForm, FormModel formModel) {
        this.subForm = subForm;
        this.formModel = formModel;
        this.panel = new FlowPanel();
        this.loader = new SubFormInstanceLoader(formModel);

        this.loadingPanel = new LoadingPanel<>();
        this.loadingPanel.setDisplayWidget(this);
        this.loadingPanel.showWithoutLoad();

        addButton = new Button(ElementStyle.LINK);
        addButton.setLabel(I18N.CONSTANTS.addAnother());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addForm(newValueInstance(), RepeatingSubFormPanel.this.panel.getWidgetIndex(addButton));
                setDeleteButtonsState();
            }
        });

        bindEvents();
    }

    private void bindEvents() {
        formModel.getEventBus().addHandler(BeforeSaveEvent.TYPE, new BeforeSaveEvent.Handler() {
            @Override
            public void handle(BeforeSaveEvent event) {
                for (SimpleFormPanel panel : forms.values()) {
                    formModel.getChangedInstances().addAll(panel.getModel().getChangedInstances());
                }
            }
        });
    }

    private Set<FormInstance> getInstances() {
        Set<FormInstance> formInstances = formModel.getSubFormInstances().get(key());
        if (formInstances == null) {
            formInstances = Sets.newHashSet(newValueInstance());
            formModel.getSubFormInstances().put(key(), formInstances);
        }
        if (formInstances.isEmpty()) {
            formInstances.add(newValueInstance());
        }
        return formInstances;
    }

    private void render() {
        List<FormInstance> instances = Lists.newArrayList(getInstances());

        Collections.sort(instances, new Comparator<FormInstance>() {
            @Override
            public int compare(FormInstance o1, FormInstance o2) {
                return getCreationTime(o1).compareTo(getCreationTime(o2));
            }
        });

        for (FormInstance instance : instances) {
            addForm(instance);
        }

        panel.add(addButton);
    }

    private static Long getCreationTime(FormInstance instance) {
        return 0L; // todo
    }

    private void addForm(final FormInstance formInstance) {
        addForm(formInstance, -1);
    }

    private FormInstance newValueInstance() {
        FormInstance newInstance = new FormInstance(ResourceId.generateSubmissionId(subForm.getId()), subForm.getId());
        newInstance.setParentRecordId(formModel.getWorkingRootInstance().getId());

        FormModel.SubformValueKey key = key();

        Set<FormInstance> instances = formModel.getSubFormInstances().get(key);
        if (instances == null) {
            instances = Sets.newHashSet();
            formModel.getSubFormInstances().put(key, instances);
        }

        instances.add(newInstance);
        formModel.getChangedInstances().add(newInstance);

        return newInstance;
    }

    private FormModel.SubformValueKey key() {
        return new FormModel.SubformValueKey(subForm, formModel.getWorkingRootInstance());
    }

    private void addForm(final FormInstance instance, int panelIndex) {

        final SimpleFormPanel formPanel = new SimpleFormPanel(formModel.getLocator(), formModel.getStateProvider());
        formPanel.asWidget().addStyleName(FormPanelStyles.INSTANCE.subformPanel());

        formPanel.addDeleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.remove(formPanel);
                forms.remove(instance);

                if (loader.isPersisted(instance)) { // schedule deletion only if instance is persisted
                    formModel.getPersistedInstanceToRemoveByLocator().add(instance);
                }

                getInstances().remove(instance);
                formModel.getChangedInstances().remove(instance);

                setDeleteButtonsState();
            }
        });

        formPanel.show(instance);

        forms.put(instance, formPanel);

        if (panelIndex == -1) {
            panel.add(formPanel);
        } else {
            panel.insert(formPanel, panelIndex);
        }

        setDeleteButtonsState();
    }

    private void setDeleteButtonsState() {
        boolean moreThanOne = forms.size() > 1;
        for (SimpleFormPanel form : forms.values()) {
            form.getDeleteButton().get().setEnabled(moreThanOne);
        }
    }

    public Map<FormInstance, SimpleFormPanel> getForms() {
        return forms;
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    public Promise<Void> show(Void value) {
        return loader.load(subForm).then(new Function<List<FormInstance>, Void>() {
            @Override
            public Void apply(List<FormInstance> input) {
                render();
                return null;
            }
        });
    }

    @Override
    public LoadingPanel<Void> getLoadingPanel() {
        return loadingPanel;
    }
}
