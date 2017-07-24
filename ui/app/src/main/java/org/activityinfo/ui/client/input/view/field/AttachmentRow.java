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

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.attachment.Attachment;

/**
 * @author yuriyz on 8/12/14.
 */
public class AttachmentRow extends Composite {

    public interface ValueChangedCallback {
        void fireValueChanged();
    }

    interface OurUiBinder extends UiBinder<HTMLPanel, AttachmentRow> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    private boolean readOnly;
    private final Attachment attachment;
    private final ResourceId resourceId;

    @UiField
    Button removeButton;
    @UiField
    AnchorElement fileName;
    @UiField
    SpanElement thumbnailContainer;
    @UiField
    HTMLPanel rootPanel;

    public AttachmentRow(Attachment attachment, ResourceId resourceId) {
        initWidget(ourUiBinder.createAndBindUi(this));

        this.attachment = attachment;
        this.resourceId = resourceId;

        setFileName();
        setThumbnail();

        rootPanel.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                removeButton.setVisible(true);
            }
        }, MouseOverEvent.getType());
        rootPanel.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                removeButton.setVisible(false);
            }
        }, MouseOutEvent.getType());

    }

    private void setFileName() {
        fileName.setInnerSafeHtml(SafeHtmlUtils.fromString(
                !Strings.isNullOrEmpty(attachment.getFilename()) ? " " + attachment.getFilename() : " " + I18N.CONSTANTS.unknown()));
        fileName.setHref(Blobs.getAttachmentUri(attachment.getBlobId(), resourceId));
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Attachment getValue() {
        return attachment;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        this.removeButton.setEnabled(!readOnly);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    private void setThumbnail() {
        if (attachment.getMimeType().contains("pdf")) {
//            thumbnailContainer.setClassName(Icons.INSTANCE.filePdf());
        } else {
//            thumbnailContainer.setClassName(Icons.INSTANCE.file());
        }
    }
}
