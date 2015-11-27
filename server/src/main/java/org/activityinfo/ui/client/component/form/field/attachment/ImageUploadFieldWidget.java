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
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.util.Holder;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.FormPanelStyles;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;

import javax.annotation.Nullable;

/**
 * @author yuriyz on 11/16/2015.
 */
public class ImageUploadFieldWidget implements FormFieldWidget<AttachmentValue> {

    interface OurUiBinder extends UiBinder<FormPanel, ImageUploadFieldWidget> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    private final FormPanel rootPanel;
    private final ValueUpdater valueUpdater;
    private final Uploader uploader;

    private boolean readOnly;
    private Holder<Attachment> attachment = Holder.of(new Attachment());
    private HandlerRegistration oldHandler;
    private String servingUrl = null;

    @UiField
    org.activityinfo.ui.client.widget.Button browseButton;
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
    Button downloadButton;
    @UiField
    com.google.gwt.user.client.ui.Button clearButton;
    @UiField
    HTMLPanel loadingContainer;
    @UiField
    HTMLPanel imageContainer;

    public ImageUploadFieldWidget(ResourceId resourceId, final ValueUpdater valueUpdater, final FieldWidgetMode fieldWidgetMode) {
        this.valueUpdater = valueUpdater;

        FormPanelStyles.INSTANCE.ensureInjected();

        rootPanel = ourUiBinder.createAndBindUi(this);

        browseButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (readOnly) {
                    Window.alert(I18N.CONSTANTS.controlIsReadOnly());
                    return;
                }
                triggerUpload(fileUpload.getElement());
            }
        });
        downloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                download();
            }
        });
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearValue();
            }
        });

        uploader = new Uploader(formPanel, fileUpload, attachment, resourceId, hiddenFieldsContainer, new Uploader.UploadCallback() {
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
                    highlight(false);
                } else {
                    Window.alert(I18N.CONSTANTS.uploadIsNotAllowedInDuringDesing());
                }
            }
        });

        addFileDnDSupport();
    }

    private void download() {
        try {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uploader.getBaseUrl() + "/blobUrl");
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    Window.open(response.getText(), "_blank", null);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Log.error("Failed to fetch image blob serving url. ", exception);
                }
            });
        } catch (RequestException e) {
            Log.error("Failed to send request for fetching image blob serving url. ", e);
        }
    }

    private void addFileDnDSupport() {
        imageContainer.addDomHandler(new DragEnterHandler() {
            @Override
            public void onDragEnter(DragEnterEvent event) {
                highlight(true);
            }
        }, DragEnterEvent.getType());

        imageContainer.addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                highlight(true);
            }
        }, DragOverEvent.getType());

        imageContainer.addDomHandler(new DragLeaveHandler() {
            @Override
            public void onDragLeave(DragLeaveEvent event) {
                highlight(false);
            }
        }, DragLeaveEvent.getType());

        imageContainer.addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                //event.preventDefault();

                // we are following the mouse with fileUpload which leads to dropping directly to input type=file
            }
        }, DropEvent.getType());
    }

    private void highlight(boolean enter) {
        if (enter) {
            imageContainer.addStyleName(FormPanelStyles.INSTANCE.dropFile());
        } else {
            imageContainer.removeStyleName(FormPanelStyles.INSTANCE.dropFile());
        }

        fileUpload.setVisible(enter);
        setLoadingState(false);
        image.setVisible(!enter);
        clearButton.setVisible(false);
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
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uploader.getBaseUrl() + "/imageUrl");
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    servingUrl = response.getText();
                    setStateAfterUpload();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    servingUrl = null;
                    Log.error("Failed to fetch image serving url. ", exception);
                    setStateAfterUpload();
                }
            });
        } catch (Exception e) {
            servingUrl = null;
            Log.error("Failed to send request for fetching serving url. ", e);
            setStateAfterUpload();
        }
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(servingUrl);
    }

    private void setStateAfterUpload() {
        setLoadingState(false);

        boolean valid = isValid();
        downloadButton.setVisible(valid);
        clearButton.setVisible(valid);
        image.setUrl(valid ? servingUrl + "=s" + formPanel.getOffsetWidth() : "");
        uploadFailed.setVisible(!valid);

        if (valid) {
            valueUpdater.update(getValue());
        } else {
            Log.error("Failed to fetch image serving url.");
        }
    }

    private AttachmentValue getValue() {
        AttachmentValue value = new AttachmentValue();
        value.getValues().add(attachment.get());
        return value;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public Promise<Void> setValue(AttachmentValue value) {
        clearValue();

        if (value != null && value.getValues() != null && value.getValues().size() > 0) {
            attachment.set(value.getValues().iterator().next());
            fetchImageServingUrl();
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
        clearButton.setVisible(false);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    public void setLoadingState(boolean loadingState) {
        loadingContainer.setVisible(loadingState);
        imageContainer.setVisible(!loadingState);
        if (loadingState) {
            image.setUrl("");
        }
    }

    private static native void triggerUpload(Element element) /*-{
        element.click();
    }-*/;
}
