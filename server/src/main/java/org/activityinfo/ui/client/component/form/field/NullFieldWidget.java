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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.promise.Promise;

/**
 * @author yuriyz on 08/21/2015.
 */
public class NullFieldWidget implements FormFieldWidget {

    public static final NullFieldWidget INSTANCE = new NullFieldWidget();

    NullFieldWidget() {
    }

    @Override
    public void fireValueChanged() {
    }

    @Override
    public void setReadOnly(boolean readOnly) {
    }

    @Override
    public Promise<Void> setValue(FieldValue value) {
        return null;
    }

    @Override
    public void setType(FieldType type) {
    }

    @Override
    public void clearValue() {
    }

    @Override
    public Widget asWidget() {
        return new HTML();
    }
}
