package org.activityinfo.ui.client.component.form.field.image;
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

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;

import java.util.List;

/**
 * @author yuriyz on 8/7/14.
 */
public class ImageUploadFieldWidget implements FormFieldWidget<AttachmentValue>, ImageUploadRow.ValueChangedCallback {

    interface OurUiBinder extends UiBinder<HTMLPanel, ImageUploadFieldWidget> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    private final HTMLPanel rootPanel;
    private final FormField formField;
    private final FieldWidgetMode fieldWidgetMode;
    private final ValueUpdater valueUpdater;

    private String resourceId;

    public ImageUploadFieldWidget(String resourceId, FormField formField, final ValueUpdater valueUpdater, FieldWidgetMode fieldWidgetMode) {
        this.resourceId = resourceId;
        this.formField = formField;
        this.fieldWidgetMode = fieldWidgetMode;
        this.valueUpdater = valueUpdater;

        rootPanel = ourUiBinder.createAndBindUi(this);

        addNewRow(new Attachment());
    }

    public void fireValueChanged() {
        valueUpdater.update(getValue());
    }

    private AttachmentValue getValue() {
        AttachmentValue value = new AttachmentValue();

        for (ImageUploadRow row : rowsFromPanel()) {
            value.getValues().add(row.getValue());
        }

        return value;
    }

    private void addNewRow(final Attachment rowValue) {
        final ImageUploadRow imageUploadRow = new ImageUploadRow(
                rowValue, formField.getId().asString(), resourceId, fieldWidgetMode, this);

        imageUploadRow.addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                addNewRow(new Attachment());
            }
        });

        imageUploadRow.removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                rootPanel.remove(imageUploadRow);
                setButtonsState();
                fireValueChanged();
            }
        });

        rootPanel.add(imageUploadRow);

        setButtonsState();
    }

    private List<ImageUploadRow> rowsFromPanel() {
        List<ImageUploadRow> rows = Lists.newArrayList();
        for (int i = 0; i < rootPanel.getWidgetCount(); i++) {
            Widget widget = rootPanel.getWidget(i);
            if (widget instanceof ImageUploadRow) rows.add((ImageUploadRow) widget);
        }
        return rows;
    }

    private void setButtonsState() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                List<ImageUploadRow> rows = rowsFromPanel();

                // Disable the button if it's the only row, so the user will not be trapped in a widget without any rows
                if (rows.size() == 1) {
                    rows.get(0).removeButton.setEnabled(false);
                } else if (rows.size() > 1) {
                    for (ImageUploadRow row : rows) {
                        row.removeButton.setEnabled(!row.isReadOnly());
                    }
                }
            }
        });
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        for (ImageUploadRow row : rowsFromPanel()) {
            row.setReadOnly(readOnly);
        }

        setButtonsState();
    }

    @Override
    public Promise<Void> setValue(AttachmentValue value) {
        if (value != null && value.getValues() != null && value.getValues().size() > 0) {
            clear();

            for (Attachment rowValue : value.getValues()) {
                addNewRow(rowValue);
            }
        } else {
            clearValue();
        }

        return Promise.done();
    }

    @Override
    public void setType(FieldType type) {
        // ignore
    }

    @Override
    public void clearValue() {
        clear();
        addNewRow(new Attachment());
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    private void clear() {
        rootPanel.clear();
    }
}
