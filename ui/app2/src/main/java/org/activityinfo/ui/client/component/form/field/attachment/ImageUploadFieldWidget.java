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

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.attachment.AttachmentValue;
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
    private final FieldWidgetMode fieldWidgetMode;

    private boolean readOnly;
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
        this.fieldWidgetMode = fieldWidgetMode;

        FormPanelStyles.INSTANCE.ensureInjected();

        rootPanel = ourUiBinder.createAndBindUi(this);
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

        imageContainer.setVisible(state == State.LOADED && fieldWidgetMode == FieldWidgetMode.NORMAL);
        image.setUrl(imageUrl);
        placeholder.setVisible(state != State.LOADED);
        this.message.setInnerText(message);
        browseLink.setVisible(state != State.LOADING && state != State.LOADED);
        browseLink.setText(state == State.FAILED ? I18N.CONSTANTS.retry() : I18N.CONSTANTS.browse());
        downloadButton.setVisible(state == State.LOADED && fieldWidgetMode == FieldWidgetMode.NORMAL);
        clearButton.setVisible(state == State.LOADED && fieldWidgetMode == FieldWidgetMode.NORMAL);
    }


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public Promise<Void> setValue(AttachmentValue value) {
        clearValue();
        return Promise.done();
    }

    @Override
    public void setType(FieldType type) {
        // ignore
    }

    @Override
    public void clearValue() {
        setState(State.NONE);
    }

    @Override
    public void fireValueChanged() {
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

}
