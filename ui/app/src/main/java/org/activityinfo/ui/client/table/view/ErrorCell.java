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
package org.activityinfo.ui.client.table.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.model.query.ErrorCode;

import java.util.Collections;
import java.util.Set;

/**
 * Shows an error in formula
 */
public class ErrorCell implements Cell<String> {
    @Override
    public boolean dependsOnSelection() {
        return false;
    }

    @Override
    public Set<String> getConsumedEvents() {
        return Collections.emptySet();
    }

    @Override
    public boolean handlesSelection() {
        return false;
    }

    @Override
    public boolean isEditing(Context context, Element parent, String value) {
        return false;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        //
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        sb.appendEscaped(ErrorCode.MISSING.getValue());
    }

    @Override
    public boolean resetFocus(Context context, Element parent, String value) {
        return false; // = focus not taken
    }

    @Override
    public void setValue(Context context, Element parent, String value) {
        //
    }
}
