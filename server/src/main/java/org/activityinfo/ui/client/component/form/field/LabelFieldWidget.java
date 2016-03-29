package org.activityinfo.ui.client.component.form.field;
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
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.MetadataType;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.promise.Promise;

/**
 * @author yuriyz on 03/29/2016.
 */
public class LabelFieldWidget implements FormFieldWidget<NullFieldValue> {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, LabelFieldWidget> {
    }

    @UiField
    HTMLPanel panel;

    public LabelFieldWidget(MetadataType metadataType) {
        uiBinder.createAndBindUi(this);

        panel.add(new HTML(SafeHtmlUtils.fromString(labelText(metadataType))));
    }

    private String labelText(MetadataType metadataType) {
        Object text = metadataType.getValues().get("text");
        return text instanceof String ? (String) text : "";
    }

    @Override
    public void setReadOnly(boolean readOnly) {
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Promise<Void> setValue(NullFieldValue value) {
        return null;
    }

    @Override
    public void setType(FieldType type) {
    }

    @Override
    public void clearValue() {
    }

    @Override
    public void fireValueChanged() {
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
