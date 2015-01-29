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
import org.activityinfo.model.form.FormSection;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerStyles;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;

/**
 * @author yuriyz on 7/14/14.
 */
public class FieldsHolderWidgetContainer implements WidgetContainer, FieldsHolder {

    private final FormDesigner formDesigner;
    private final FormElementContainer elementContainer;
    private final FieldsHolderPanel panel;
    private final ResourceId parentId;

    protected FieldsHolderWidgetContainer(final FormDesigner formDesigner, final FormElementContainer elementContainer, ResourceId parentId) {
        this.formDesigner = formDesigner;
        this.elementContainer = elementContainer;
        this.parentId = parentId;

        panel = new FieldsHolderPanel(formDesigner, parentId);
        panel.getPanel().setClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDesigner.getContainerPresenter().show(FieldsHolderWidgetContainer.this);
                formDesigner.getEventBus().fireEvent(new WidgetContainerSelectionEvent(FieldsHolderWidgetContainer.this));
            }
        });
        formDesigner.getEventBus().addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                WidgetContainer selectedItem = event.getSelectedItem();
                if (selectedItem instanceof FieldsHolderWidgetContainer) {
                    panel.getPanel().setSelected(selectedItem.asWidget().equals(panel.asWidget()));
                }
            }
        });

        panel.getPanel().getWidgetContainer().add(createDropPanel());
        syncWithModel();
    }

    public static FieldsHolderWidgetContainer section(final FormDesigner formDesigner, final FormSection formSection, ResourceId parentId) {
        FieldsHolderWidgetContainer container = new FieldsHolderWidgetContainer(formDesigner, formSection, parentId);
        container.getPanel().getPanel().getRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDesigner.getRootFormClass().remove(formSection);
                formDesigner.getDropControllerRegistry().unregister(formSection.getId());
            }
        });
        return container;
    }

    public static FieldsHolderWidgetContainer subform(final FormDesigner formDesigner, final FormClass formClass, ResourceId parentId) {
        FieldsHolderWidgetContainer container = new FieldsHolderWidgetContainer(formDesigner, formClass, parentId);
        container.getPanel().getPanel().getRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDesigner.getModel().removeSubform(formClass);
                formDesigner.getDropControllerRegistry().unregister(formClass.getId());
            }
        });
        return container;
    }

    public FieldsHolderPanel getPanel() {
        return panel;
    }

    public ResourceId getParentId() {
        return parentId;
    }

    private Widget createDropPanel() {
        FlowPanel dropPanel = new FlowPanel();
        dropPanel.addStyleName(FormDesignerStyles.INSTANCE.sectionWidgetContainer());

        formDesigner.getDropControllerRegistry().register(elementContainer.getId(), dropPanel, formDesigner);

        return dropPanel ;
    }

    public void syncWithModel() {
        panel.getPanel().getLabel().setHTML("<h3>" + SafeHtmlUtils.fromString(Strings.nullToEmpty(elementContainer.getLabel())).asString() + "</h3>");
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
    public void updateUi() {
        syncWithModel();
    }
}