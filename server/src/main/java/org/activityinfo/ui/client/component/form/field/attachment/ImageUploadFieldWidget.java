package org.activityinfo.ui.client.component.form.field.attachment;
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

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.widget.Button;

/**
 * @author yuriyz on 11/16/2015.
 */
public class ImageUploadFieldWidget implements FormFieldWidget<AttachmentValue> {

    interface OurUiBinder extends UiBinder<HTMLPanel, ImageUploadFieldWidget> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    private final HTMLPanel rootPanel;
    private final FormField formField;
    private final FieldWidgetMode fieldWidgetMode;
    private final ValueUpdater valueUpdater;

    private Attachment attachment = new Attachment();

    @UiField
    Button browseButton;

    public ImageUploadFieldWidget(String resourceId, FormField formField, final ValueUpdater valueUpdater, FieldWidgetMode fieldWidgetMode) {
        this.formField = formField;
        this.fieldWidgetMode = fieldWidgetMode;
        this.valueUpdater = valueUpdater;

        rootPanel = ourUiBinder.createAndBindUi(this);
    }

    public void fireValueChanged() {
        valueUpdater.update(getValue());
    }

    private AttachmentValue getValue() {
        AttachmentValue value = new AttachmentValue();
        value.getValues().add(attachment);
        return value;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        browseButton.setEnabled(!readOnly);
    }

    @Override
    public Promise<Void> setValue(AttachmentValue value) {
        clearValue();

        if (value != null && value.getValues() != null && value.getValues().size() > 0) {
            this.attachment = value.getValues().iterator().next();
        }

        return Promise.done();
    }

    @Override
    public void setType(FieldType type) {
        // ignore
    }

    @Override
    public void clearValue() {
        // todo
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }
}
