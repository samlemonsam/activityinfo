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
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.drop.DropControllerRegistry;
import org.activityinfo.ui.client.component.formdesigner.header.HeaderPanel;
import org.activityinfo.ui.client.component.formdesigner.properties.ContainerPropertiesPresenter;
import org.activityinfo.ui.client.component.formdesigner.properties.FieldEditor;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author yuriyz on 07/07/2014.
 */
public class FormDesigner {

    private final EventBus eventBus = new SimpleEventBus();
    private final StateProvider stateProvider;
    private final ResourceLocator resourceLocator;
    private final ContainerPropertiesPresenter containerPresenter;
    private final FormDesignerPanel formDesignerPanel;
    private final FormFieldWidgetFactory formFieldWidgetFactory;
    private final FormSavedGuard savedGuard;
    private final FormDesignerActions formDesignerActions;
    private final DropControllerRegistry dropControllerRegistry;
    private final FormDesignerModel model;

    public FormDesigner(@Nonnull ResourceLocator resourceLocator, @Nonnull FormClass rootFormClass, @Nonnull StateProvider stateProvider) {
        this.resourceLocator = resourceLocator;
        this.stateProvider = stateProvider;

        this.model = new FormDesignerModel(rootFormClass);
        this.dropControllerRegistry = new DropControllerRegistry(this);
        this.formDesignerPanel = new FormDesignerPanel(rootFormClass, this);
        this.formDesignerPanel.getFieldPalette().makeDraggable(dropControllerRegistry.getDragController());

        containerPresenter = new ContainerPropertiesPresenter(this);
        formDesignerPanel.getFieldEditor().start(this);
        formDesignerPanel.getHeaderPanel().start(this);

        formFieldWidgetFactory = new FormFieldWidgetFactory(resourceLocator, FieldWidgetMode.DESIGN);

        dropControllerRegistry.register(rootFormClass.getId(), formDesignerPanel.getDropPanel(), this);

        formDesignerPanel.bind(eventBus);
        model.bind(eventBus);


        savedGuard = new FormSavedGuard(this);

        formDesignerActions = FormDesignerActions.create(this);
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

    public FieldEditor getPropertiesPanel() {
        return formDesignerPanel.getFieldEditor();
    }

    public HeaderPanel getHeaderPresenter() {
        return formDesignerPanel.getHeaderPanel();
    }

    public FormDesignerModel getModel() {
        return model;
    }

    public StateProvider getStateProvider() {
        return stateProvider;
    }

    public WidgetContainer getWidgetContainer(ResourceId resourceId) {
        Map<ResourceId, WidgetContainer> map = getFormDesignerPanel().getContainerMap();
        if (map.containsKey(resourceId)) {
            return map.get(resourceId);
        }
        return dropControllerRegistry.getDropController(resourceId).getContainerMap().get(resourceId);
    }

    public static Set<ResourceId> builtinFields(ResourceId formClassId) {
        Set<ResourceId> fieldIds = new HashSet<>();
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.START_DATE_FIELD));
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.END_DATE_FIELD));
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.COMMENT_FIELD));
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.PARTNER_FIELD));
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.PROJECT_FIELD));
        return fieldIds;
    }

    public static boolean isBuiltin(ResourceId formClassId, ResourceId fieldId) {
        return builtinFields(formClassId).contains(fieldId);
    }

    public static boolean cannotBeRemoved(ResourceId formClassId, ResourceId fieldId) {
        if (fieldId.equals(CuidAdapter.field(formClassId, CuidAdapter.PARTNER_FIELD))) {
            return true;
        }
        return false;
    }


    public static boolean isPartner(FormField field) {
        if(field.getType() instanceof ReferenceType) {
            ReferenceType type = (ReferenceType) field.getType();
            if(type.getRange().size() == 1) {
                return type.getRange().iterator().next().getDomain() == CuidAdapter.PARTNER_FORM_CLASS_DOMAIN;
            }
        }
        return false;
    }
}
