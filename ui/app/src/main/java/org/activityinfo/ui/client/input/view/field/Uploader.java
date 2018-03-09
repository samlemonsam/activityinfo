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
package org.activityinfo.ui.client.input.view.field;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.attachment.Attachment;

import javax.annotation.Nullable;

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
        uploadViaAppEngine();
    }

    private void uploadViaAppEngine() {
        hiddenFieldsContainer.clear();

        newAttachment();

        hiddenFieldsContainer.add(new Hidden("blobId", attachment.getBlobId()));
        hiddenFieldsContainer.add(new Hidden("fileName", attachment.getFilename()));
        hiddenFieldsContainer.add(new Hidden("mimeType", attachment.getMimeType()));
        hiddenFieldsContainer.add(new Hidden("resourceId", resourceId.asString()));

        formPanel.setAction("/service/appengine");
        formPanel.setMethod("POST");
        uploadCallback.upload();
    }

    private void newAttachment() {
        String blobId = ResourceId.generateId().asString();
        String fileName = filename();
        String mimeType = mimeType();

        attachment.setMimeType(mimeType);
        attachment.setFilename(fileName);
        attachment.setBlobId(blobId);
    }

    public String getBaseUrl() {
        return getBaseUrl(attachment.getBlobId(), resourceId);
    }


    private String filename() {
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

    private String mimeType() {
        return mimeType(fileUpload.getElement().cast());
    }

    private static native String mimeType(InputElement element) /*-{
        return element.files[0].type;
    }-*/;


    public static String getBaseUrl(String blobId, ResourceId resourceId) {
        return "/service/blob/" + blobId + "/" + resourceId.asString();
    }
}
