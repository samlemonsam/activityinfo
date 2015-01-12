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
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerConstants;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;

/**
 * @author yuriyz on 7/14/14.
 */
public class FieldWidgetContainer implements WidgetContainer {

    public interface LabelTemplate extends SafeHtmlTemplates {
        @Template("<span style='color: red;'> *</span>")
        SafeHtml mandatoryMarker();
    }

    private static final LabelTemplate LABEL_TEMPLATE = GWT.create(LabelTemplate.class);

    private FormDesigner formDesigner;
    private FormFieldWidget formFieldWidget;
    private FormField formField;
    private final FieldPanel fieldPanel;
    private final ResourceId parentId;

    public FieldWidgetContainer(final FormDesigner formDesigner, FormFieldWidget formFieldWidget, final FormField formField, ResourceId parentId) {
        this.formDesigner = formDesigner;
        this.formFieldWidget = formFieldWidget;
        this.formField = formField;
        this.parentId = parentId;

        fieldPanel = new FieldPanel(formDesigner);
        fieldPanel.getWidgetContainer().add(formFieldWidget);
        fieldPanel.getRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDesigner.getFormClass().remove(formField);
            }
        });
        fieldPanel.setClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDesigner.getEventBus().fireEvent(new WidgetContainerSelectionEvent(FieldWidgetContainer.this));
            }
        });
        this.formDesigner.getEventBus().addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                WidgetContainer selectedItem = event.getSelectedItem();
                if (selectedItem instanceof FieldWidgetContainer) {
                    fieldPanel.setSelected(selectedItem.asWidget().equals(fieldPanel.asWidget()));
                }
            }
        });

        // Workaround(alex): store field id with widget so we can update model order after
        // drag and drop
        fieldPanel.asWidget().getElement().setAttribute(FormDesignerConstants.DATA_FIELD_ID, formField.getId().asString());
        syncWithModel();
    }

    @Override
    public ResourceId getParentId() {
        return parentId;
    }

    public void syncWithModel() {
        final SafeHtmlBuilder label = new SafeHtmlBuilder();

        if (!Strings.isNullOrEmpty(formField.getCode())) { // append code
            label.appendHtmlConstant("<span class='small'>" + SafeHtmlUtils.fromString(formField.getCode()).asString() + "</span>&nbsp;");
        }

        label.append(SafeHtmlUtils.fromString(Strings.nullToEmpty(formField.getLabel())));
        if (formField.isRequired()) {
            label.append(LABEL_TEMPLATE.mandatoryMarker());
        }
        formFieldWidget.setReadOnly(formField.isReadOnly());

        String labelHtml = label.toSafeHtml().asString();
        if (!formField.isVisible()) {
            labelHtml = "<del>" + labelHtml + "</del>";
        }
        fieldPanel.getLabel().setHTML(labelHtml);
        formFieldWidget.setType(formField.getType());
    }

    public Widget asWidget() {
        return fieldPanel.asWidget();
    }

    public Widget getDragHandle() {
        return fieldPanel.getDragHandle();
    }

    public FormFieldWidget getFormFieldWidget() {
        return formFieldWidget;
    }

    public FormField getFormField() {
        return formField;
    }

    public FormDesigner getFormDesigner() {
        return formDesigner;
    }
}
