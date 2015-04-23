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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.image.ImageRowValue;
import org.activityinfo.model.type.image.ImageValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;

import java.util.List;

/**
 * @author yuriyz on 8/7/14.
 */
public class ImageUploadFieldWidget implements FormFieldWidget<ImageValue> {

    interface OurUiBinder extends UiBinder<HTMLPanel, ImageUploadFieldWidget> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    private final HTMLPanel rootPanel;
    private final FormField formField;
    private String resourceId;
    private ImageValue value = new ImageValue();

    public ImageUploadFieldWidget(String resourceId, FormField formField, final ValueUpdater valueUpdater) {
        this.resourceId = resourceId;
        this.formField = formField;
        rootPanel = ourUiBinder.createAndBindUi(this);

        addNewRow(new ImageRowValue());
    }

    private void addNewRow(final ImageRowValue rowValue) {
        final ImageUploadRow imageUploadRow = new ImageUploadRow(rowValue, formField.getId().asString(), resourceId);
        imageUploadRow.addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                addNewRow(new ImageRowValue());
            }
        });

        imageUploadRow.removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                value.getValues().remove(imageUploadRow.getValue());
                rootPanel.remove(imageUploadRow);
                setButtonsState();
            }
        });

        value.getValues().add(rowValue);
        rootPanel.add(imageUploadRow);

        setButtonsState();
    }

    private void setButtonsState() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                List<ImageUploadRow> rows = Lists.newArrayListWithCapacity(rootPanel.getWidgetCount());
                for (int i = 0; i < rootPanel.getWidgetCount(); i++) {
                    Widget widget = rootPanel.getWidget(i);
                    if (widget instanceof ImageUploadRow) rows.add((ImageUploadRow) widget);
                }

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
        for (int i = 0; i < rootPanel.getWidgetCount(); i++) {
            IsWidget widget = rootPanel.getWidget(i);
            if (widget instanceof ImageUploadRow) {
                ImageUploadRow row = (ImageUploadRow) widget;
                row.setReadOnly(readOnly);
            }
        }

        setButtonsState();
    }

    @Override
    public Promise<Void> setValue(ImageValue value) {
        if (value != null && value.getValues() != null && value.getValues().size() > 0) {
            clear();

            for (ImageRowValue rowValue : value.getValues()) {
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
        addNewRow(new ImageRowValue());
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    private void clear() {
        value = new ImageValue();
        rootPanel.clear();
    }
}
