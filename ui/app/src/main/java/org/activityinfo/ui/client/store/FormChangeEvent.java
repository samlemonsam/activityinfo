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
package org.activityinfo.ui.client.store;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Signals that a Form, its schema, or records has changed.
 */
public class FormChangeEvent extends GwtEvent<FormChangeEventHandler> {
    public static Type<FormChangeEventHandler> TYPE = new Type<FormChangeEventHandler>();

    public Type<FormChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    private final FormChange change;

    public FormChangeEvent(FormChange change) {
        this.change = change;
    }

    public FormChange getChange() {
        return change;
    }

    protected void dispatch(FormChangeEventHandler handler) {
        handler.onFormChange(this);
    }
}
