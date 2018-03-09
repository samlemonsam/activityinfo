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

import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.Dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Dialogs {

    /**
     * Adds a one-time callback for this dialog that is called when the OK button is clicked.
     */
    public static <T> void addCallback(Dialog dialog, Consumer<T> callback, Supplier<T> supplier) {
        List<HandlerRegistration> registrations = new ArrayList<>();

        registrations.add(dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(event -> {
            dialog.hide();
            callback.accept(supplier.get());
        }));
        registrations.add(dialog.addDialogHideHandler(event -> {
            for (HandlerRegistration registration : registrations) {
                registration.removeHandler();
            }
            registrations.clear();
        }));
    }

}
