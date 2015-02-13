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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.drop.DropControllerRegistry;
import org.activityinfo.ui.client.component.formdesigner.header.HeaderPresenter;
import org.activityinfo.ui.client.component.formdesigner.properties.ContainerPropertiesPresenter;
import org.activityinfo.ui.client.component.formdesigner.properties.PropertiesPresenter;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author yuriyz on 07/07/2014.
 */
public class FormDesigner {

    private final EventBus eventBus = new SimpleEventBus();
    private final ResourceLocator resourceLocator;
    private final PropertiesPresenter propertiesPresenter;
    private final ContainerPropertiesPresenter containerPresenter;
    private final HeaderPresenter headerPresenter;
    private final FormDesignerPanel formDesignerPanel;
    private final FormFieldWidgetFactory formFieldWidgetFactory;
    private final FormSavedGuard savedGuard;
    private final FormDesignerActions formDesignerActions;
    private final DropControllerRegistry dropControllerRegistry;
    private final FormDesignerModel model;

    public FormDesigner(@Nonnull ResourceLocator resourceLocator, @Nonnull FormClass rootFormClass) {
        this.resourceLocator = resourceLocator;

        this.model = new FormDesignerModel(rootFormClass);
        this.dropControllerRegistry = new DropControllerRegistry(this);
        this.formDesignerPanel = new FormDesignerPanel(rootFormClass, this);
        this.formDesignerPanel.getFieldPalette().makeDraggable(dropControllerRegistry.getDragController());

        containerPresenter = new ContainerPropertiesPresenter(this);
        propertiesPresenter = new PropertiesPresenter(this);

        formFieldWidgetFactory = new FormFieldWidgetFactory(resourceLocator, FieldWidgetMode.DESIGN);

        dropControllerRegistry.register(rootFormClass.getId(), formDesignerPanel.getDropPanel(), this);

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

    public FormClass getRootFormClass() {
        return getModel().getRootFormClass();
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

    public FormDesignerModel getModel() {
        return model;
    }

    public WidgetContainer getWidgetContainer(ResourceId resourceId) {
        Map<ResourceId, WidgetContainer> map = getFormDesignerPanel().getContainerMap();
        if (map.containsKey(resourceId)) {
            return map.get(resourceId);
        }
        return dropControllerRegistry.getDropController(resourceId).getContainerMap().get(resourceId);
    }
}
