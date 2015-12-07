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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
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
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.util.Http;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.FormPanelStyles;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;

/**
 * @author yuriyz on 11/16/2015.
 */
public class ImageUploadFieldWidget implements FormFieldWidget<AttachmentValue> {

    interface OurUiBinder extends UiBinder<FormPanel, ImageUploadFieldWidget> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    private enum State {
        NONE, FAILED, LOADING, LOADED
    }

    private final FormPanel rootPanel;
    private final ValueUpdater valueUpdater;
    private final Uploader uploader;

    private boolean readOnly;
    private HandlerRegistration oldHandler;
    private String servingUrl = null;

    @UiField
    Anchor browseLink;
    @UiField
    FileUpload fileUpload;
    @UiField
    Image image;
    @UiField
    FormPanel formPanel;
    @UiField
    VerticalPanel hiddenFieldsContainer;
    @UiField
    Button downloadButton;
    @UiField
    com.google.gwt.user.client.ui.Button clearButton;
    @UiField
    HTMLPanel placeholder;
    @UiField
    SpanElement message;
    @UiField
    HTMLPanel imageContainer;

    public ImageUploadFieldWidget(ResourceId resourceId, final ValueUpdater valueUpdater, final FieldWidgetMode fieldWidgetMode) {
        this.valueUpdater = valueUpdater;

        FormPanelStyles.INSTANCE.ensureInjected();

        rootPanel = ourUiBinder.createAndBindUi(this);

        browseLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
                if (!readOnly) {
                    triggerUpload(fileUpload.getElement());
                }
            }
        });
        downloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open(uploader.getPermanentLink(), "_blank", null);
            }
        });
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearValue();
                fireValueChanged();
            }
        });

        uploader = new Uploader(formPanel, fileUpload, resourceId, hiddenFieldsContainer, new Uploader.UploadCallback() {
            @Override
            public void onFailure(Throwable exception) {
                setState(State.FAILED);
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
                    setState(State.LOADING);
                    uploader.upload();
                }
            }
        });
    }

//    private void addFileDnDSupport() {
//        imageContainer.addDomHandler(new DragEnterHandler() {
//            @Override
//            public void onDragEnter(DragEnterEvent event) {
//                highlight(true);
//            }
//        }, DragEnterEvent.getType());
//
//        imageContainer.addDomHandler(new DragOverHandler() {
//            @Override
//            public void onDragOver(DragOverEvent event) {
//                highlight(true);
//            }
//        }, DragOverEvent.getType());
//
//        imageContainer.addDomHandler(new DragLeaveHandler() {
//            @Override
//            public void onDragLeave(DragLeaveEvent event) {
//                highlight(false);
//            }
//        }, DragLeaveEvent.getType());
//
//        imageContainer.addDomHandler(new DropHandler() {
//            @Override
//            public void onDrop(DropEvent event) {
//                //event.preventDefault();
//
//                // we are following the mouse with fileUpload which leads to dropping directly to input type=file
//            }
//        }, DropEvent.getType());
//    }

//    private void highlight(boolean enter) {
//        if (enter) {
//            imageContainer.addStyleName(FormPanelStyles.INSTANCE.dropFile());
//        } else {
//            imageContainer.removeStyleName(FormPanelStyles.INSTANCE.dropFile());
//        }
//
//        fileUpload.setVisible(enter);
//        setLoadingState(false);
//        image.setVisible(!enter);
//        clearButton.setVisible(false);
//    }

    private void upload() {
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
                    if (response.getStatusCode() == Http.Status.OK.getCode()) {
                        servingUrl = response.getText();
                        setState(State.LOADED);
                    } else {
                        setState(State.FAILED);
                    }

                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Log.error("Failed to fetch image serving url. ", exception);
                    setState(State.FAILED);
                }
            });
        } catch (Exception e) {
            Log.error("Failed to send request for fetching serving url. ", e);
            setState(State.FAILED);
        }
    }

    private void setState(State state) {
        String imageUrl = "";
        String message = "";

        switch (state) {
            case FAILED:
                message = I18N.CONSTANTS.uploadFailed();
                servingUrl = null;
                break;
            case LOADED:
                imageUrl = servingUrl + "=s" + formPanel.getOffsetWidth();

                fireValueChanged();
                break;
            case LOADING:
                message = I18N.CONSTANTS.loading();
                servingUrl = null;
                break;
            case NONE:
                message = I18N.CONSTANTS.noImage();
                servingUrl = null;
                formPanel.reset(); // forced to clear file input state
                break;
        }

        image.setUrl(imageUrl);
        placeholder.setVisible(state != State.LOADED);
        this.message.setInnerText(message);
        browseLink.setVisible(state != State.LOADING && state != State.LOADED);
        browseLink.setText(state == State.FAILED ? I18N.CONSTANTS.retry() : I18N.CONSTANTS.browse());
        downloadButton.setVisible(state == State.LOADED);
        clearButton.setVisible(state == State.LOADED);
    }

    private void fireValueChanged() {
        AttachmentValue attachmentValue = new AttachmentValue();
        attachmentValue.getValues().add(uploader.getAttachment());
        valueUpdater.update(attachmentValue);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public Promise<Void> setValue(AttachmentValue value) {
        clearValue();

        if (value != null && value.getValues() != null && value.getValues().size() > 0) {
            uploader.setAttachment(value.getValues().iterator().next());
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
        uploader.setAttachment(new Attachment());
        setState(State.NONE);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    private static native void triggerUpload(Element element) /*-{
        element.click();
    }-*/;
}
