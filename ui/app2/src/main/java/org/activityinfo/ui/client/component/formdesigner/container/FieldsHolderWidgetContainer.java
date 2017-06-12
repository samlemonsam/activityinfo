package org.activityinfo.ui.client.component.formdesigner.container;
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

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElementContainer;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormSection;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.form.subform.PeriodTabStrip;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerStyles;
import org.activityinfo.ui.client.component.formdesigner.drop.DropControllerExtended;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;

/**
 * @author yuriyz on 7/14/14.
 */
public class FieldsHolderWidgetContainer implements WidgetContainer, FieldsHolder {

    private final FormDesigner formDesigner;
    private final FormElementContainer elementContainer;
    private final FieldsHolderPanel panel;
    private final ResourceId parentId;
    private boolean isSubform = false;
    private DropControllerExtended dropController;
    private FormField subFormField;

    protected FieldsHolderWidgetContainer(final FormDesigner formDesigner, final FormElementContainer elementContainer, ResourceId parentId) {
        this.formDesigner = formDesigner;
        this.elementContainer = elementContainer;
        this.parentId = parentId;

        panel = new FieldsHolderPanel(formDesigner, parentId);
        panel.getPanel().setClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (FieldsHolderWidgetContainer.this.equals(formDesigner.getModel().getSelectedWidgetContainer())) {
                    return; // skip, container is already selected
                }

                formDesigner.getContainerPresenter().show(FieldsHolderWidgetContainer.this);
                formDesigner.getEventBus().fireEvent(new WidgetContainerSelectionEvent(FieldsHolderWidgetContainer.this));
            }
        });
        formDesigner.getEventBus().addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                WidgetContainer selectedItem = event.getSelectedItem();
                panel.getPanel().setSelected(selectedItem.asWidget().equals(panel.asWidget()));
            }
        });

        FlowPanel dropPanel = createDropPanel();
        panel.getPanel().getSubformContainer().add(dropPanel);
        dropController = formDesigner.getDropControllerRegistry().register(elementContainer.getId(), dropPanel, formDesigner);
        dropController.getContainerMap().put(elementContainer.getId(), this); // register yourself

        syncWithModel();
    }

    public static FieldsHolderWidgetContainer section(final FormDesigner formDesigner, final FormSection formSection, final ResourceId parentId) {
        FieldsHolderWidgetContainer container = new FieldsHolderWidgetContainer(formDesigner, formSection, parentId);
        container.getPanel().getPanel().setOnRemoveConfirmationCallback(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                formDesigner.getModel().getFormClass(parentId).remove(formSection);
                formDesigner.getDropControllerRegistry().unregister(formSection.getId());
                formDesigner.getContainerPresenter().reset();
            }
        });

        return container;
    }

    public static FieldsHolderWidgetContainer subform(final FormDesigner formDesigner, FormField formField, final FormClass subForm, ResourceId parentId) {
        FieldsHolderWidgetContainer container = new FieldsHolderWidgetContainer(formDesigner, subForm, parentId);
        container.isSubform = true;
        container.subFormField = formField;
        container.getPanel().getPanel().setOnRemoveConfirmationCallback(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                formDesigner.getModel().removeSubform(subForm);
                formDesigner.getDropControllerRegistry().unregister(subForm.getId());
                formDesigner.getContainerPresenter().reset();
            }
        });

        container.getPanel().getPanel().getWidgetContainer().setVisible(false);
        if (subForm.getSubFormKind().isPeriod()) {
            container.getPanel().getPanel().getSubformTabContainer().add(new PeriodTabStrip(subForm.getSubFormKind()));
        }
        container.syncWithModel(); // force ui update
        return container;
    }

    public FieldsHolderPanel getPanel() {
        return panel;
    }

    public ResourceId getParentId() {
        return parentId;
    }

    private FlowPanel createDropPanel() {
        FlowPanel dropPanel = new FlowPanel();
        dropPanel.addStyleName(FormDesignerStyles.INSTANCE.sectionWidgetContainer());
        return dropPanel;
    }

    public void syncWithModel(final boolean force) {
        panel.getPanel().getLabel().setHTML("<h3>" + SafeHtmlUtils.fromString(Strings.nullToEmpty(elementContainer.getLabel())).asString() + "</h3>");
    }

    @Override
    public void syncWithModel() {
        syncWithModel(true);
    }

    public Widget asWidget() {
        return panel.asWidget();
    }

    @Override
    public Widget getDragHandle() {
        return panel.getDragHandle();
    }

    public FormDesigner getFormDesigner() {
        return formDesigner;
    }

    @Override
    public FormElementContainer getElementContainer() {
        return elementContainer;
    }

    @Override
    public void setLabel(String label) {
        if(subFormField != null) {
            subFormField.setLabel(label);
            elementContainer.setLabel(label);
        }
    }

    @Override
    public void updateUi() {
        syncWithModel();
    }

    public DropControllerExtended getDropController() {
        return dropController;
    }

    public boolean isSubform() {
        return isSubform;
    }
}