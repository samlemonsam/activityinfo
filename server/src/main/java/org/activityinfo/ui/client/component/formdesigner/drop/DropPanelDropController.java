package org.activityinfo.ui.client.component.formdesigner.drop;
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

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.drop.FlowPanelDropController;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.container.FieldsHolderWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.event.PanelUpdatedEvent;
import org.activityinfo.ui.client.component.formdesigner.palette.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 07/07/2014.
 */
public class DropPanelDropController extends FlowPanelDropController implements DropControllerExtended {

    private final Positioner positioner = new Positioner();
    private final ResourceId resourceId;
    private FormDesigner formDesigner;
    private FlowPanel dropTarget;
    private final Map<ResourceId, WidgetContainer> containerMap = Maps.newHashMap();

    public DropPanelDropController(ResourceId resourceId, FlowPanel dropTarget, FormDesigner formDesigner) {
        super(dropTarget);
        this.resourceId = resourceId;
        this.formDesigner = formDesigner;
        this.dropTarget = dropTarget;
    }

    @Override
    public void onPreviewDrop(final DragContext context) throws VetoDragException {
        super.onPreviewDrop(context); // important ! - calculates drop index

        if (context.draggable instanceof DnDLabel) {
            previewDropNewWidget(context);
        } else {
            drop(context.draggable, context);

            // update model
            Scheduler.get().scheduleDeferred(new Command() {
                @Override
                public void execute() {
                    updateModel(context.selectedWidgets.get(0), context.finalDropController != null ? context.finalDropController : context.dropController);
                    removePositioner();
                }
            });
        }
    }

    private List<WidgetContainer> getAllContainers() {
        List<WidgetContainer> containers = new ArrayList<>(containerMap.values());
        containers.addAll(formDesigner.getFormDesignerPanel().getContainerMap().values());
        for (DropControllerExtended c : formDesigner.getDropControllerRegistry().getDropControllers()) {
            containers.addAll(c.getContainerMap().values());
        }
        return containers;
    }

    private boolean isField(Widget draggable) {
        for (WidgetContainer container : getAllContainers()) {
            if (draggable.equals(container.asWidget()) && container instanceof FieldWidgetContainer) {
                return true;
            }
        }
        return false;
    }

    private void updateModel(Widget draggable, DropController dropController) {
        DropPanelDropController panelDropController = (DropPanelDropController) dropController;


        for (WidgetContainer container : getAllContainers()) {
            if (draggable.equals(container.asWidget())) {
                if (container instanceof FieldWidgetContainer) {
                    FormField formField = ((FieldWidgetContainer) container).getFormField();

                    removeFromSrouceFormClass(formField);

                    insertIntoTargetFormClass(panelDropController, container, formField);

                } else if (container instanceof FieldsHolderWidgetContainer) {

                    FieldsHolderWidgetContainer fieldsHolderContainer = (FieldsHolderWidgetContainer) container;

                    if (fieldsHolderContainer.isSubform()) { // subform
                        FormClass subForm = (FormClass) fieldsHolderContainer.getElementContainer();
                        FormField subformOwnerField = formDesigner.getModel().getSubformOwnerField(subForm);

                        removeFromSrouceFormClass(subformOwnerField);

                        insertIntoTargetFormClass(panelDropController, container, subformOwnerField);

                    } else { // form section
                        FormSection formSection = (FormSection) fieldsHolderContainer.getElementContainer();
                        FormClass sourceFormClass = formDesigner.getModel().getFormClassByElementId(formSection.getId());
                        sourceFormClass.remove(formSection);

                        insertIntoTargetFormClass(panelDropController, container, formSection);
                    }

                }
            }
        }
    }

    private void removeFromSrouceFormClass(FormElement formElement) {
        FormClass sourceFormClass = formDesigner.getModel().getFormClassByElementId(formElement.getId());
        sourceFormClass.remove(formElement);
    }

    private void insertIntoTargetFormClass(DropPanelDropController panelDropController, WidgetContainer container, FormElement formElement) {
        int widgetIndex = panelDropController.getDropTarget().getWidgetIndex(container.asWidget());

        // target form class
        FormElementContainer elementContainer = formDesigner.getModel().getElementContainer(panelDropController.getResourceId());

        // update model
        elementContainer.insertElement(widgetIndex, formElement);
    }

    private void previewDropNewWidget(final DragContext context) throws VetoDragException {
        final Template template = ((DnDLabel) context.draggable).getTemplate();
        if (template instanceof FieldTemplate) {
            final FormField formField = ((FieldTemplate)template).create();
            FormClass formClass = formDesigner.getModel().getFormClassByElementId(resourceId);
            formDesigner.getFormFieldWidgetFactory().createWidget(formClass, formField, NullValueUpdater.INSTANCE).then(new Function<FormFieldWidget, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable FormFieldWidget formFieldWidget) {
                    final FieldWidgetContainer fieldWidgetContainer = new FieldWidgetContainer(formDesigner, formFieldWidget, formField, resourceId);
                    containerMap.put(formField.getId(), fieldWidgetContainer);
                    drop(fieldWidgetContainer, context, formField);

                    return null;
                }
            });
        } else if (template instanceof SectionTemplate) {
            final FormSection formSection = ((SectionTemplate)template).create();

            vetoDropIfNeeded(context);

            FieldsHolderWidgetContainer widgetContainer = FieldsHolderWidgetContainer.section(formDesigner, formSection, resourceId);
            containerMap.put(resourceId, widgetContainer); // parent drop container
            drop(widgetContainer, context, formSection);
        } else if (template instanceof SubformTemplate) {
            final FormField formField = ((SubformTemplate)template).create();

            vetoDropIfNeeded(context);

            FormClass subForm = formDesigner.getModel().registerNewSubform(formField.getId());
            subForm.setLabel(formField.getLabel());

            SubFormType type = (SubFormType) formField.getType();
            type.getClassReference().setRange(subForm.getId());

            final FieldsHolderWidgetContainer widgetContainer = FieldsHolderWidgetContainer.subform(formDesigner, subForm, formField.getId());
            containerMap.put(formField.getId(), widgetContainer); // parent drop container

            drop(widgetContainer.asWidget(), context);
            int widgetIndex = dropTarget.getWidgetIndex(widgetContainer.asWidget());
            formDesigner.getModel().getElementContainer(resourceId).insertElement(widgetIndex, formField);

            formDesigner.getEventBus().fireEvent(new PanelUpdatedEvent(widgetContainer, PanelUpdatedEvent.EventType.ADDED));
            formDesigner.getDragController().makeDraggable(widgetContainer.asWidget(), widgetContainer.getDragHandle());

            removePositioner();
        }


        // forbid drop of source control widget
        throw new VetoDragException();
    }

    private void vetoDropIfNeeded(DragContext context) throws VetoDragException {
        // DnDLabel then drop is always allowed
        if (context.draggable instanceof DnDLabel && ((DnDLabel) context.draggable).getTemplate() instanceof FieldTemplate) {
            return;
        }
        if (isField(context.selectedWidgets.get(0))) { // field is moved
            return;
        }

        if (formDesigner.getModel().getElementContainer(resourceId) instanceof FormSection ||
                formDesigner.getModel().isSubform(formDesigner.getModel().getElementContainer(resourceId).getId())) {
            // we are not going to handle nested FormSection or nested SubForms in FormDesigner
            // It should be enough to handle one level of FormSections and SubForms:
            // 1. on selection FormSection/SubForm container is selected by blue color
            // 2. on formField selection highlight it with green color
            // nested FormSection/SubForm brings higher complexity without comparative value.
            throw new VetoDragException();
        }
    }

    private void drop(final Widget widget, DragContext context) {
        // hack ! - replace original selected widget with our container, drop it and then restore selection
        final List<Widget> originalSelectedWidgets = context.selectedWidgets;
        context.selectedWidgets = Lists.newArrayList(widget);
        DropPanelDropController.super.onDrop(context); // drop container
        context.selectedWidgets = originalSelectedWidgets; // restore state;

        formDesigner.getSavedGuard().setSaved(false);
    }

    private void drop(final WidgetContainer widgetContainer, DragContext context, final FormElement formElement) {
        drop(widgetContainer.asWidget(), context);

        formDesigner.getEventBus().fireEvent(new PanelUpdatedEvent(widgetContainer, PanelUpdatedEvent.EventType.ADDED));
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                int widgetIndex = dropTarget.getWidgetIndex(widgetContainer.asWidget());

                FormElementContainer elementContainer = formDesigner.getModel().getElementContainer(resourceId);

                // update model
                elementContainer.insertElement(widgetIndex, formElement);
                formDesigner.getDragController().makeDraggable(widgetContainer.asWidget(), widgetContainer.getDragHandle());

                removePositioner();
            }
        });
    }

    @Override
    protected Widget newPositioner(DragContext context) {
        return positioner.asWidget();
    }

    @Override
    public void setPositionerToEnd() {
        removePositioner();
        dropTarget.insert(positioner, (dropTarget.getWidgetCount()));
    }

    private void removePositioner() {
        int currentIndex = dropTarget.getWidgetIndex(positioner);
        if (currentIndex != -1) {
            dropTarget.remove(currentIndex);
        }
    }

    @Override
    public void onEnter(DragContext context) {
        super.onEnter(context);

        try {
            positioner.setForbidded(false);
            vetoDropIfNeeded(context);

        } catch (VetoDragException e) {
            positioner.setForbidded(true);
        }
    }

    public Map<ResourceId, WidgetContainer> getContainerMap() {
        return containerMap;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public FlowPanel getDropTarget() {
        return dropTarget;
    }
}
