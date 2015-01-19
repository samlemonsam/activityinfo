package org.activityinfo.ui.client.component.formdesigner;
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

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElement;
import org.activityinfo.model.form.FormElementContainer;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.drop.DropControllerRegistry;
import org.activityinfo.ui.client.component.formdesigner.header.HeaderPresenter;
import org.activityinfo.ui.client.component.formdesigner.properties.ContainerPropertiesPresenter;
import org.activityinfo.ui.client.component.formdesigner.properties.PropertiesPresenter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 07/07/2014.
 */
public class FormDesigner {

    private final EventBus eventBus = new SimpleEventBus();
    private final ResourceLocator resourceLocator;
    private final FormClass formClass;
    private final PropertiesPresenter propertiesPresenter;
    private final ContainerPropertiesPresenter containerPresenter;
    private final HeaderPresenter headerPresenter;
    private final FormDesignerPanel formDesignerPanel;
    private final FormFieldWidgetFactory formFieldWidgetFactory;
    private final FormSavedGuard savedGuard;
    private final FormDesignerActions formDesignerActions;
    private final DropControllerRegistry dropControllerRegistry;

    public FormDesigner(@Nonnull ResourceLocator resourceLocator, @Nonnull FormClass formClass) {
        this.resourceLocator = resourceLocator;
        this.formClass = formClass;

        this.dropControllerRegistry = new DropControllerRegistry(this);
        this.formDesignerPanel = new FormDesignerPanel(resourceLocator, formClass, this);
        this.formDesignerPanel.getFieldPalette().makeDraggable(dropControllerRegistry.getDragController());

        containerPresenter = new ContainerPropertiesPresenter(this);
        propertiesPresenter = new PropertiesPresenter(this);

        formFieldWidgetFactory = new FormFieldWidgetFactory(resourceLocator, FieldWidgetMode.DESIGN);

        dropControllerRegistry.register(formClass.getId(), formDesignerPanel.getDropPanel(), this);

        formDesignerPanel.bind(eventBus);

        headerPresenter = new HeaderPresenter(this);
        headerPresenter.show();

        savedGuard = new FormSavedGuard(this);

        formDesignerActions = new FormDesignerActions(this);
    }

    public DropControllerRegistry getDropControllerRegistry() {
        return dropControllerRegistry;
    }

    public FormDesignerActions getFormDesignerActions() {
        return formDesignerActions;
    }

    public FormSavedGuard getSavedGuard() {
        return savedGuard;
    }

    public FormDesignerPanel getFormDesignerPanel() {
        return formDesignerPanel;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }

    public FormClass getFormClass() {
        return formClass;
    }

    public FormFieldWidgetFactory getFormFieldWidgetFactory() {
        return formFieldWidgetFactory;
    }

    public PickupDragController getDragController() {
        return dropControllerRegistry.getDragController();
    }

    public ContainerPropertiesPresenter getContainerPresenter() {
        return containerPresenter;
    }

    public PropertiesPresenter getPropertiesPresenter() {
        return propertiesPresenter;
    }

    public HeaderPresenter getHeaderPresenter() {
        return headerPresenter;
    }

    public FormElementContainer getElementContainer(ResourceId resourceId) {
        return getFormClass().getElementContainer(resourceId);
    }

    public WidgetContainer getWidgetContainer(ResourceId resourceId) {
        Map<ResourceId, WidgetContainer> map = getFormDesignerPanel().getContainerMap();
        if (map.containsKey(resourceId)) {
            return map.get(resourceId);
        }
        return dropControllerRegistry.getDropController(resourceId).getContainerMap().get(resourceId);
    }

    public void updateFieldOrder() {

        Map<ResourceId, FormField> fieldMap = Maps.newHashMap();
        for (FormField field : formClass.getFields()) {
            fieldMap.put(field.getId(), field);
        }

        // update the order of the model
        List<FormElement> elements = Lists.newArrayList();
        FlowPanel panel = formDesignerPanel.getDropPanel();
        for (int i = 0; i != panel.getWidgetCount(); ++i) {
            Widget widget = panel.getWidget(i);
            String fieldId = widget.getElement().getAttribute(FormDesignerConstants.DATA_FIELD_ID);
            elements.add(fieldMap.get(ResourceId.valueOf(fieldId)));
        }

        formClass.getElements().clear();
        formClass.getElements().addAll(elements);
    }
}
