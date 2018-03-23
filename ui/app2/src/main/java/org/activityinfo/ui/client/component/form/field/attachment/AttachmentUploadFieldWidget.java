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
package org.activityinfo.ui.client.component.form.field.attachment;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.model.resource.ResourceId;
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
public class AttachmentUploadFieldWidget implements FormFieldWidget<AttachmentValue>, AttachmentUploadRow.ValueChangedCallback {

    interface OurUiBinder extends UiBinder<HTMLPanel, AttachmentUploadFieldWidget> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    @UiField
    HTMLPanel rootPanel;
    @UiField
    HTMLPanel noAttachments;
    @UiField
    VerticalPanel hiddenFieldsContainer;
    @UiField
    FormPanel formPanel;
    @UiField
    HTMLPanel uploadFailed;
    @UiField
    AnchorElement browseButton;
    @UiField
    FileUpload fileUpload;
    @UiField
    HTMLPanel loadingContainer;
    @UiField
    ImageElement loadingImage;
    @UiField
    HTMLPanel rows;

    private final ValueUpdater valueUpdater;
    private final ResourceId resourceId;

    private HandlerRegistration oldHandler;
    private boolean readOnly;

    public AttachmentUploadFieldWidget(ResourceId resourceId, final ValueUpdater valueUpdater, final FieldWidgetMode fieldWidgetMode) {
        this.resourceId = resourceId;
        this.valueUpdater = valueUpdater;

        ourUiBinder.createAndBindUi(this);

        Event.sinkEvents(browseButton, Event.ONCLICK);
        Event.setEventListener(browseButton, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                event.preventDefault();
                if (!readOnly && fieldWidgetMode == FieldWidgetMode.NORMAL) {
                    triggerUpload(fileUpload.getElement());
                }
            }
        });

    }

    private void upload() {
        loadingContainer.setVisible(true);
        uploadFailed.setVisible(false);
        noAttachments.setVisible(false);

        if (oldHandler != null) {
            oldHandler.removeHandler();
        }

        oldHandler = formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                // event.getResults is always null because of cross-domain upload
                // we are forced to make additional call to check whether upload is successful

                formPanel.reset();
            }
        });
        formPanel.submit();
    }


    private void setState(boolean success) {
        loadingContainer.setVisible(false);

        int size = rowsFromPanel().size();

        noAttachments.setVisible(size == 0);
        uploadFailed.setVisible(!success);
    }

    private static native void triggerUpload(Element element) /*-{
        element.click();
    }-*/;

    public void fireValueChanged() {
        valueUpdater.update(getValue());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private AttachmentValue getValue() {
        AttachmentValue value = new AttachmentValue();

        for (AttachmentUploadRow row : rowsFromPanel()) {
            value.getValues().add(row.getValue());
        }

        return value;
    }

    private void addNewRow(final Attachment attachment) {
        final AttachmentUploadRow uploadRow = new AttachmentUploadRow(attachment, resourceId);

        uploadRow.removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                rows.remove(uploadRow);
                setState(true);
                fireValueChanged();
            }
        });

        rows.add(uploadRow);
    }

    private List<AttachmentUploadRow> rowsFromPanel() {
        List<AttachmentUploadRow> rows = Lists.newArrayList();
        for (int i = 0; i < this.rows.getWidgetCount(); i++) {
            Widget widget = this.rows.getWidget(i);
            if (widget instanceof AttachmentUploadRow) {
                rows.add((AttachmentUploadRow) widget);
            }
        }
        return rows;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;

        for (AttachmentUploadRow row : rowsFromPanel()) {
            row.setReadOnly(readOnly);
        }
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public Promise<Void> setValue(AttachmentValue value) {
        if (value != null && value.getValues() != null && value.getValues().size() > 0) {
            rows.clear();

            for (Attachment rowValue : value.getValues()) {
                addNewRow(rowValue);
            }
            setState(true);
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
        rows.clear();
        formPanel.reset();
        setState(true);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }
}
