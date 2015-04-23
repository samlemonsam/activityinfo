package org.activityinfo.ui.client.component.formdesigner.properties;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.ui.client.widget.CheckBox;
import org.activityinfo.ui.client.widget.TextArea;
import org.activityinfo.ui.client.widget.TextBox;
import org.activityinfo.ui.client.widget.form.FormGroup;

/**
 * @author yuriyz on 7/9/14.
 */
public class PropertiesPanel extends Composite {

    private static OurUiBinder uiBinder = GWT
            .create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, PropertiesPanel> {
    }

    @UiField
    TextBox label;
    @UiField
    HTMLPanel panel;
    @UiField
    TextArea description;
    @UiField
    CheckBox required;
    @UiField
    HTMLPanel requiredGroup;
    @UiField
    HTMLPanel readOnlyGroup;
    @UiField
    CheckBox readOnly;
    @UiField
    Button relevanceButton;
    @UiField
    HTMLPanel relevanceGroup;
    @UiField
    SpanElement relevanceExpression;
    @UiField
    CheckBox visible;
    @UiField
    FormGroup visibleGroup;
    @UiField
    RadioButton relevanceEnabled;
    @UiField
    RadioButton relevanceEnabledIf;
    @UiField
    TextBox code;
    @UiField
    FormGroup codeGroup;
    @UiField
    FormGroup labelGroup;

    public PropertiesPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public TextBox getLabel() {
        return label;
    }

    public HTMLPanel getPanel() {
        return panel;
    }

    public TextArea getDescription() {
        return description;
    }

    public CheckBox getRequired() {
        return required;
    }

    public HTMLPanel getRequiredGroup() {
        return requiredGroup;
    }

    public HTMLPanel getReadOnlyGroup() {
        return readOnlyGroup;
    }

    public CheckBox getReadOnly() {
        return readOnly;
    }

    public Button getRelevanceButton() {
        return relevanceButton;
    }

    public HTMLPanel getRelevanceGroup() {
        return relevanceGroup;
    }

    public SpanElement getRelevanceExpression() {
        return relevanceExpression;
    }

    public CheckBox getVisible() {
        return visible;
    }

    public FormGroup getVisibleGroup() {
        return visibleGroup;
    }

    public RadioButton getRelevanceEnabled() {
        return relevanceEnabled;
    }

    public RadioButton getRelevanceEnabledIf() {
        return relevanceEnabledIf;
    }

    public TextBox getCode() {
        return code;
    }

    public FormGroup getCodeGroup() {
        return codeGroup;
    }

    public FormGroup getLabelGroup() {
        return labelGroup;
    }
}
