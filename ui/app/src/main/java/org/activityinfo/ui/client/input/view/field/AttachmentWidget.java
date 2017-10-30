package org.activityinfo.ui.client.input.view.field;
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
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yuriyz on 8/7/14.
 */
public class AttachmentWidget implements FieldWidget, AttachmentRow.ValueChangedCallback {

    private static final Logger LOGGER = Logger.getLogger(AttachmentWidget.class.getName());

    interface OurUiBinder extends UiBinder<HTMLPanel, AttachmentWidget> {
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
    TextButton browseButton;
    @UiField
    FileUpload fileUpload;
    @UiField
    HTMLPanel loadingContainer;

    @UiField
    HTMLPanel rows;

    private final FieldUpdater valueUpdater;
    private final ResourceId formId;
    private final Uploader uploader;

    private HandlerRegistration oldHandler;

    public AttachmentWidget(ResourceId formId, final FieldUpdater valueUpdater) {
        this.formId = formId;
        this.valueUpdater = valueUpdater;

        ourUiBinder.createAndBindUi(this);

        this.uploader = new Uploader(formPanel, fileUpload, formId, hiddenFieldsContainer, new Uploader.UploadCallback() {
            @Override
            public void onFailure(Throwable exception) {
                uploadFailed.setVisible(true);
            }

            @Override
            public void upload() {
                AttachmentWidget.this.upload();
            }
        });

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                uploader.setAttachment(new Attachment());
                uploader.upload();
            }
        });
    }

    @UiHandler("browseButton")
    public void onBrowse(SelectEvent event) {
        triggerUpload(fileUpload.getElement());
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

                checkBlobExistance();
                formPanel.reset();
            }
        });
        formPanel.submit();
    }

    public void checkBlobExistance() {
        try {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uploader.getBaseUrl() + "/exists");
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        addNewRow(uploader.getAttachment());

                        setState(true);
                        fireValueChanged();
                    } else {
                        LOGGER.severe("Failed to fetch attachment serving url. Status code is not ok. ");
                        setState(false);
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    LOGGER.log(Level.SEVERE, "Failed to fetch attachment serving url. ", exception);
                    setState(false);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send request for fetching serving url. ", e);
            setState(false);
        }
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
        List<AttachmentRow> rows = rowsFromPanel();
        if(rows.isEmpty()) {
            valueUpdater.update(FieldInput.EMPTY);
        } else {
            valueUpdater.update(new FieldInput(getValue()));
        }
    }


    @Override
    public void init(FieldValue value) {
        if(value instanceof AttachmentValue) {
            setValue(((AttachmentValue) value));
        } else {
            clear();
        }
    }


    @Override
    public void setRelevant(boolean relevant) {
        for (AttachmentRow row : rowsFromPanel()) {
            row.setReadOnly(!relevant);
        }
    }

    private AttachmentValue getValue() {
        AttachmentValue value = new AttachmentValue();
        for (AttachmentRow row : rowsFromPanel()) {
            value.getValues().add(row.getValue());
        }
        return value;
    }

    private void addNewRow(final Attachment attachment) {
        final AttachmentRow uploadRow = new AttachmentRow(attachment, formId);

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

    private List<AttachmentRow> rowsFromPanel() {
        List<AttachmentRow> rows = new ArrayList<>();
        for (int i = 0; i < this.rows.getWidgetCount(); i++) {
            Widget widget = this.rows.getWidget(i);
            if (widget instanceof AttachmentRow) {
                rows.add((AttachmentRow) widget);
            }
        }
        return rows;
    }

    public void setValue(AttachmentValue value) {
        if (value != null && value.getValues() != null && value.getValues().size() > 0) {
            rows.clear();

            for (Attachment rowValue : value.getValues()) {
                addNewRow(rowValue);
            }
            setState(true);
        } else {
            clear();
        }
    }

    @Override
    public void clear() {
        rows.clear();
        formPanel.reset();
        setState(true);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }
}
