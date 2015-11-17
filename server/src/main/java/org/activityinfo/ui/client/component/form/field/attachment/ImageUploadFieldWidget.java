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

import com.google.common.base.Strings;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.util.Holder;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.widget.Button;

import javax.annotation.Nullable;

/**
 * @author yuriyz on 11/16/2015.
 */
public class ImageUploadFieldWidget implements FormFieldWidget<AttachmentValue> {

    interface OurUiBinder extends UiBinder<FormPanel, ImageUploadFieldWidget> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    private static final int IMAGE_HEIGHT = 200;

    private final FormPanel rootPanel;
    private final ValueUpdater valueUpdater;
    private final Uploader uploader;

    private Holder<Attachment> attachment = Holder.of(new Attachment());
    private HandlerRegistration oldHandler;
    private String servingUrl = null;

    @UiField
    Button browseButton;
    @UiField
    FileUpload fileUpload;
    @UiField
    Image image;
    @UiField
    FormPanel formPanel;
    @UiField
    VerticalPanel hiddenFieldsContainer;
    @UiField
    HTMLPanel uploadFailed;
    @UiField
    com.google.gwt.user.client.ui.Button downloadButton;
    @UiField
    com.google.gwt.user.client.ui.Button clearButton;
    @UiField
    HTMLPanel loadingContainer;

    public ImageUploadFieldWidget(final ValueUpdater valueUpdater, final FieldWidgetMode fieldWidgetMode) {
        this.valueUpdater = valueUpdater;

        rootPanel = ourUiBinder.createAndBindUi(this);

        browseButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                triggerUpload(fileUpload.getElement());
            }
        });
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearValue();
            }
        });
        uploader = new Uploader(formPanel, fileUpload, attachment, hiddenFieldsContainer, new Uploader.UploadCallback() {
            @Override
            public void onFailure(@Nullable Throwable exception) {
                uploadFailed.setVisible(true);
            }

            @Override
            public void upload() {
                ImageUploadFieldWidget.this.upload();
            }
        });
        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fieldWidgetMode == FieldWidgetMode.NORMAL) {
                    uploader.requestUploadUrl();
                } else {
                    Window.alert(I18N.CONSTANTS.uploadIsNotAllowedInDuringDesing());
                }
            }
        });
    }

    private void upload() {
        downloadButton.setVisible(false);
        uploadFailed.setVisible(false);
        setLoadingState(true);

        if (oldHandler != null) {
            oldHandler.removeHandler();
        }

        oldHandler = formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                // event.getResults is always null because of cross-domain upload
                // we are forced to make additional call to check whether upload is successful

                fetchImageServingUrl();
            }
        });
        formPanel.submit();
    }

    public void fetchImageServingUrl() {
        try {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uploader.getBaseUrl() +
                    "/image_url?image_size=" + IMAGE_HEIGHT);
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    servingUrl = response.getText();
                    setUploadState();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Log.error("Failed to fetch image serving url. ", exception);
                    setUploadState();
                }
            });
        } catch (RequestException e) {
            Log.error("Failed to send request for fetching serving url. ", e);
            setUploadState();
        }
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(servingUrl);
    }

    private void setUploadState() {
        setLoadingState(false);

        boolean valid = isValid();
        downloadButton.setVisible(valid);
        clearButton.setVisible(valid);
        image.setUrl(valid ? servingUrl : "");
        uploadFailed.setVisible(!valid);

        if (valid) {
            fireValueChanged();
        } else {
            Log.error("Failed to fetch image serving url.");
        }
    }

    public void fireValueChanged() {
        valueUpdater.update(getValue());
    }

    private AttachmentValue getValue() {
        AttachmentValue value = new AttachmentValue();
        value.getValues().add(attachment.get());
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
            attachment.set(value.getValues().iterator().next());
        }

        return Promise.done();
    }

    @Override
    public void setType(FieldType type) {
        // ignore
    }

    @Override
    public void clearValue() {
        image.setUrl("");
        servingUrl = null;
        attachment.set(new Attachment());
        uploadFailed.setVisible(false);
        downloadButton.setVisible(false);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    private void onLoadImageFailure() {
        uploadFailed.setVisible(true);
        clearValue();
    }

    public void setLoadingState(boolean loadingState) {
        loadingContainer.setVisible(loadingState);
        image.setVisible(!loadingState);
    }

    private static native void triggerUpload(Element element) /*-{
        element.click();
    }-*/;

    private void loadImage(JavaScriptObject event) {
        loadImage(event, image.getElement());
    }

    /**
     * Uses either URL.createObjectURL or the Files API to load the selected file
     * into the image element.
     */
    private native void loadImage(JavaScriptObject event, Element imageElement) /*-{
        var files = event.target.files;
        if (files && files.length > 0) {
            var file = files[0];
            try {
                var URL = $wnd.URL || $wnd.webkitURL;
                var imgURL = URL.createObjectURL(file);
                imageElement.src = imgURL;
                URL.revokeObjectURL(imgURL);
            }
            catch (e) {
                try {
                    var fileReader = new FileReader();
                    fileReader.onload = function (event) {
                        imageElement.src = event.target.result;
                    };
                    fileReader.readAsDataURL(file);
                }
                catch (e) {
                    this.@org.activityinfo.ui.client.component.form.field.attachment.ImageUploadFieldWidget::onLoadImageFailure()();
                }
            }
        }
    }-*/;
}
