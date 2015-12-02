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
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.activityinfo.core.shared.util.MimeTypeUtil;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.service.blob.UploadCredentials;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author yuriyz on 11/16/2015.
 */
public class Uploader {

    public interface UploadCallback {
        void onFailure(@Nullable Throwable exception);

        void upload();
    }

    private final FileUpload fileUpload;
    private final FormPanel formPanel;
    private final VerticalPanel hiddenFieldsContainer;
    private final UploadCallback uploadCallback;

    private final ResourceId resourceId;
    private Attachment attachment = new Attachment();

    public Uploader(FormPanel formPanel, FileUpload fileUpload, ResourceId resourceId,
                    VerticalPanel hiddenFieldsContainer, UploadCallback uploadCallback) {
        this.formPanel = formPanel;
        this.fileUpload = fileUpload;
        this.resourceId = resourceId;
        this.hiddenFieldsContainer = hiddenFieldsContainer;
        this.uploadCallback = uploadCallback;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public void upload() {
        try {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, URL.encode(createUploadUrl()));
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {

                    Resource resource = Resources.resourceFromJson(response.getText());
                    UploadCredentials uploadCredentials = UploadCredentials.fromRecord(resource);

                    hiddenFieldsContainer.clear();

                    Map<String, String> formFields = uploadCredentials.getFormFields();
                    for (Map.Entry<String, String> field : formFields.entrySet()) {
                        hiddenFieldsContainer.add(new Hidden(field.getKey(), field.getValue()));
                    }

                    formPanel.setAction(uploadCredentials.getUrl());
                    formPanel.setMethod(uploadCredentials.getMethod());
                    uploadCallback.upload();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Log.error("Failed to send request", exception);
                    uploadCallback.onFailure(exception);
                }
            });
        } catch (Exception e) {
            Log.error("Failed to send request", e);
            uploadCallback.onFailure(e);
        }
    }

    private String createUploadUrl() {
        String blobId = ResourceId.generateId().asString();
        String fileName = fileName();
        String mimeType = MimeTypeUtil.mimeTypeFromFileName(fileName, "application/octet-stream");

        attachment.setMimeType(mimeType);
        attachment.setFilename(fileName);
        attachment.setBlobId(blobId);

        return "/service/blob/credentials/" + blobId + "/" + resourceId.asString() + "/" + fileName;
    }

    public String getBaseUrl() {
        return getBaseUrl(attachment.getBlobId(), resourceId);
    }

    public static String getBaseUrl(String blobId, ResourceId resourceId) {
        return "/service/blob/" + blobId + "/" + resourceId.asString();
    }

    private String fileName() {
        final String filename = fileUpload.getFilename();
        if (Strings.isNullOrEmpty(filename)) {
            return "unknown";
        }

        int i = filename.lastIndexOf("/");
        if (i == -1) {
            i = filename.lastIndexOf("\\");
        }
        if (i != -1 && (i + 1) < filename.length()) {
            return filename.substring(i + 1);
        }
        return filename;
    }
}
