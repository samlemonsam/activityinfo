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
package org.activityinfo.ui.client.component.formdesigner.drop;

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
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.ui.client.component.chooseForm.ChooseFormCallback;
import org.activityinfo.ui.client.component.chooseForm.ChooseFormDialog;
import org.activityinfo.ui.client.component.form.field.FieldUpdater;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerConstants;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.container.FieldsHolderWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.container.LabelWidgetContainer;
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
            vetoDropIfNeeded(context);
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

                    removeFromSourceFormClass(formField);

                    insertIntoTargetFormClass(panelDropController, container, formField);

                } else if (container instanceof FieldsHolderWidgetContainer) {

                    FieldsHolderWidgetContainer fieldsHolderContainer = (FieldsHolderWidgetContainer) container;

                    if (fieldsHolderContainer.isSubform()) { // subform
                        FormClass subForm = (FormClass) fieldsHolderContainer.getElementContainer();
                        FormField subformOwnerField = formDesigner.getModel().getSubformOwnerField(subForm);

                        removeFromSourceFormClass(subformOwnerField);

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

    private void removeFromSourceFormClass(FormElement formElement) {
        FormClass sourceFormClass = formDesigner.getModel().getFormClassByElementId(formElement.getId());
        sourceFormClass.remove(formElement);
    }

    private void insertIntoTargetFormClass(DropPanelDropController panelDropController, WidgetContainer container, FormElement formElement) {
        // target form class
        FormClass formClass = formDesigner.getModel().getFormClass(panelDropController.getResourceId());

        int widgetIndex = panelDropController.getDropTarget().getWidgetIndex(container.asWidget());
        int formElementIndex = determineFormElementIndex(widgetIndex, formClass);

        // update model
        formClass.insertElement(formElementIndex, formElement);
    }

    /**
     * There exists a discrepancy between the Form Element index and the Widget index for keyed subforms (Monthly,
     * Daily, etc.). The period field on a keyed subform is included on the form element list, but is *not* included
     * as a Widget on the subform drop panel (this is handled specially elsewhere).
     *
     * Therefore, when we simply use the widgetIndex when reinserting the element via FormClass::insertElement, we are
     * in fact offset by -1 from the correct Form Element index. This led to the experience of not being able to move a
     * form field to the bottom of a subform in the form designer, or the field appearing above where it was intended.
     *
     * Further, when a field is moved to the top of a subform, it was able to move the index of the period key field
     * itself (as the inserted element was index 0 and pushed the period field down the list). As the issue went
     * unnoticed and unresolved for a significant period of time, an unknown number of form schema have been affected
     * and we must assume the period field could now reside anywhere on a form element list.
     *
     * Therefore, we find the correct form element index by incrementing the widgetIndex by +1 *IF*:
     *  - The form is a keyed subform
     *  - The index of the period key form element is less than or equal to the current widgetIndex
     */
    private int determineFormElementIndex(int widgetIndex, FormClass formClass) {
        if (!formClass.isSubForm()) {
            return widgetIndex;
        }
        if (formClass.getSubFormKind() == SubFormKind.REPEATING) {
            return widgetIndex;
        }

        List<FormElement> formElements = formDesigner.getModel().getElementContainer(formClass.getId()).getElements();

        // Find the index of the period field
        int i = 0;
        while (!formElements.get(i).getId().equals(ResourceId.valueOf("period")) && i < formElements.size()) {
            i++;
        }

        // If the period field is less than or equal to the proposed drop index for our widget,
        // then we must increment the widget index by 1 to get our field element index
        if (i <= widgetIndex) {
            return widgetIndex + 1;
        }
        return widgetIndex;
    }

    private void previewDropNewWidget(final DragContext context) throws VetoDragException {
        final Template template = ((DnDLabel) context.draggable).getTemplate();

        if (template instanceof FieldTemplate) {
            final FormField formField = ((FieldTemplate)template).create();

            vetoDropIfNeeded(context);

            FormClass formClass = formDesigner.getModel().getFormClassByElementId(resourceId);
            formDesigner.getFormFieldWidgetFactory().createWidget(formClass, formField, new FieldUpdater() {
                @Override
                public void onInvalid(String errorMessage) {
                }

                @Override
                public void update(Object value) {
                    formDesigner.getSavedGuard().setSaved(false);
                }
            }).then(new Function<FormFieldWidget, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable FormFieldWidget formFieldWidget) {
                    final FieldWidgetContainer fieldWidgetContainer = new FieldWidgetContainer(formDesigner, formFieldWidget, formField, resourceId);
                    containerMap.put(formField.getId(), fieldWidgetContainer);
                    drop(fieldWidgetContainer, context, formField);
                    initFieldProperties(fieldWidgetContainer, formField);
                    return null;
                }
            });
        } else if (template instanceof SectionTemplate) {
            final FormSection formSection = ((SectionTemplate)template).create();

            vetoDropIfNeeded(context);

            FieldsHolderWidgetContainer widgetContainer = FieldsHolderWidgetContainer.section(formDesigner, formSection, resourceId);
            containerMap.put(resourceId, widgetContainer); // parent drop container
            drop(widgetContainer, context, formSection);

        } else if (template instanceof LabelTemplate) {
            final FormLabel formLabel = ((LabelTemplate)template).create();

            final LabelWidgetContainer fieldWidgetContainer = new LabelWidgetContainer(formDesigner, formLabel, resourceId);
            containerMap.put(formLabel.getId(), fieldWidgetContainer);
            drop(fieldWidgetContainer, context, formLabel);

        } else if (template instanceof SubFormTemplate) {
            final FormField formField = ((SubFormTemplate)template).create();

            vetoDropIfNeeded(context);

            FormClass subForm = formDesigner.getModel().registerNewSubform(
                    formField.getId(), 
                    ((SubFormTemplate) template).getKind());
            subForm.setDatabaseId(formDesigner.getRootFormClass().getDatabaseId());
            subForm.setParentFormId(formDesigner.getRootFormClass().getId());
            subForm.setLabel(formField.getLabel());
            

            SubFormReferenceType type = (SubFormReferenceType) formField.getType();
            type.setClassId(subForm.getId());

            final FieldsHolderWidgetContainer widgetContainer = FieldsHolderWidgetContainer.subform(formDesigner, formField, subForm, formField.getId());
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


    private void initFieldProperties(FieldWidgetContainer fieldWidgetContainer, FormField formField) {
        if(formField.getType() instanceof ReferenceType) {
            chooseReference(fieldWidgetContainer, formField);
        }
    }

    private void chooseReference(final FieldWidgetContainer container, final FormField formField) {
        final ChooseFormDialog dialog = new ChooseFormDialog(this.formDesigner.getResourceLocator());
        dialog.choose(new ChooseFormCallback() {
            @Override
            public void onChosen(CatalogEntry entry) {
                ReferenceType type = (ReferenceType) formField.getType();
                type.setRange(ResourceId.valueOf(entry.getId()));
                formField.setLabel(entry.getLabel());
                container.syncWithModel();
            }

            @Override
            public void onCanceled() {
                container.removeFromForm();
            }
        });
    }

    private void vetoDropIfNeeded(DragContext context) throws VetoDragException {
        // DnDLabel then drop is always allowed
        if (context.draggable instanceof DnDLabel && ((DnDLabel) context.draggable).getTemplate() instanceof FieldTemplate) {
            return;
        }
        if (isField(context.selectedWidgets.get(0))) { // field is moved
            // Once placed, fields can only be moved within the same form/subform
            ResourceId targetFormId = getId(dropTarget, FormDesignerConstants.OWNER_ID);
            ResourceId dataFieldId = getId(context.draggable, FormDesignerConstants.DATA_FIELD_ID);

            FormClass targetForm = formDesigner.getModel().getFormClass(targetFormId);

            if (hasField(targetForm, dataFieldId)) {
                return;
            } else {
                throw new VetoDragException();
            }
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

    private ResourceId getId(Widget widget, String attributeType) {
        String id = widget.getElement().getAttribute(attributeType);
        return id != null ? ResourceId.valueOf(id) : null;
    }

    private boolean hasField(FormClass form, ResourceId fieldId) {
        try {
            FormField field = form.getField(fieldId);
            return field != null;
        } catch (IllegalArgumentException e) {
            return false;
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
