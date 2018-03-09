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
package org.activityinfo.ui.client.widget;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Event;

/**
 * Subclass of {@link RadioButton} that applies our application styles
 */
public class RadioButton extends com.google.gwt.user.client.ui.RadioButton {

    @UiConstructor
    public RadioButton(String name) {
        super(name);
        setStyleName("radio");
    }

    public RadioButton(String name, String label) {
        super(name, label);
        setStyleName("radio");
    }

    public RadioButton(String name, SafeHtml label) {
        super(name, label);
        setStyleName("radio");
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        if (event.getCtrlKey() && event.getTypeInt() == Event.ONCLICK && getValue()) {
            setValue(false, true);
        }
    }
}
