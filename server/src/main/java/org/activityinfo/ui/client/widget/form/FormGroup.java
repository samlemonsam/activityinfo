package org.activityinfo.ui.client.widget.form;
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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.Iterator;

/**
 * @author yuriyz on 11/10/2014.
 */
public class FormGroup extends Composite implements HasWidgets.ForIsWidget {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, FormGroup> {
    }

    @UiField
    LabelElement label;
    @UiField
    HTMLPanel widget;
    @UiField
    HTMLPanel messageContainer;
    @UiField
    SpanElement validationMessage;
    @UiField
    SpanElement validationMargin;
    @UiField
    SpanElement description;
    @UiField
    SpanElement descriptionMargin;
    @UiField
    HTMLPanel formGroup;

    private boolean skipWidgetColStyle = false;
    private ValidationStateType stateType = ValidationStateType.NONE;

    @UiConstructor
    public FormGroup() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public FormGroup label(String label) {
        if (!Strings.isNullOrEmpty(label)) {
            this.label.setInnerText(label);
        }
        return this;
    }

    public FormGroup description(String description) {
        if (!Strings.isNullOrEmpty(description)) {
            this.description.addClassName("help-block"); // add help-block dynamically, we don't want div to take space if description is not set
            this.description.setInnerText(description);
        }
        return this;
    }

    public FormGroup columnWidgetWidth(int width) {
        this.widget.addStyleName(GridCol.col(width));
        skipWidgetColStyle = true;
        return this;
    }

    public FormGroup columnLabelWidth(final int width) {
        if (width < 0 || width > 12) {
            throw new IllegalArgumentException("Bootstrap width is invalid. Must be in range of 0..12. Width: " + width);
        }

        this.label.addClassName(GridCol.col(width));

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                if (!skipWidgetColStyle) {
                    FormGroup.this.widget.addStyleName(GridCol.remainingCol(width));
                }
            }
        });

        this.descriptionMargin.addClassName(GridCol.col(width));
        this.description.addClassName(GridCol.remainingCol(width));

        this.validationMargin.addClassName(GridCol.col(width));
        this.validationMessage.addClassName(GridCol.remainingCol(width));
        return this;
    }

    public FormGroup validationMessage(String validationMessage) {
        this.validationMessage.setInnerHTML(SafeHtmlUtils.fromString(validationMessage).asString());
        return this;
    }

    public FormGroup showValidationMessage(String validationMessage) {
        return validationMessage(validationMessage).showValidationMessage(true);
    }

    public FormGroup showValidationMessage(boolean show) {

        if (show) {
            validationMessage.addClassName("help-block");
            validationStateType(stateType);
        } else {
            validationMessage.removeClassName("help-block");
            resetFormGroupStyles();
        }

        messageContainer.setVisible(show);
        return this;
    }

    public FormGroup validationStateType(ValidationStateType stateType) {
        this.stateType = stateType;

        resetFormGroupStyles();

        if (stateType == ValidationStateType.NONE) {
            return this;
        }

        // set style
        switch (stateType) {
            case ERROR:
                formGroup.addStyleName("has-error");
                break;
            case SUCCESS:
                formGroup.addStyleName("has-success");
                break;
            case WARNING:
                formGroup.addStyleName("has-warning");
                break;
        }
        return this;
    }

    private void resetFormGroupStyles() {
        formGroup.removeStyleName("has-error");
        formGroup.removeStyleName("has-warning");
        formGroup.removeStyleName("has-success");
    }

    public static FormGroup newInstance() {
        return new FormGroup();
    }

    public FormGroup addWidget(IsWidget w) {
        add(w);
        return this;
    }

    @Override
    public void add(IsWidget w) {
        widget.add(w);
    }

    @Override
    public void add(Widget w) {
        widget.add(w);
    }

    @Override
    public void clear() {
        widget.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return widget.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return widget.remove(w);
    }

    @Override
    public boolean remove(IsWidget w) {
        return widget.remove(w);
    }

    public void setValidationMessage(String validationMessage) { // method for UiBinder
        validationMessage(validationMessage);
    }

    public void setValidationStateType(String type) {  // method for UiBinder
        validationStateType(ValidationStateType.valueOf(type));
    }

    public void setShowValidationMessage(boolean show) { // method for UiBinder
        showValidationMessage(show);
    }

    public void setLabel(String label) {  // method for UiBinder
        label(label);
    }

    public void setColumnLabelWidth(int width) {  // method for UiBinder
        columnLabelWidth(width);
    }

    public void setColumnWidgetWidth(int width) { // method for UiBinder
        columnWidgetWidth(width);
    }

    public void setDescription(String description) { // method for UiBinder
        description(description);
    }

}
