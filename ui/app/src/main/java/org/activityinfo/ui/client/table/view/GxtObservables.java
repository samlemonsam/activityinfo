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
import com.sencha.gxt.widget.core.client.form.Field;
import org.activityinfo.observable.Observable;

/**
 * Adapter methods between GXT views and observables
 */
public class GxtObservables {

    public static <T> Observable<T> of(Field<T> field) {
        return new Observable<T>() {

            private HandlerRegistration handlerRegistration;

            @Override
            public boolean isLoading() {
                return field.getValue() == null;
            }

            @Override
            public T get() {
                return field.getValue();
            }

            @Override
            protected void onConnect() {
                super.onConnect();
                handlerRegistration = field.addValueChangeHandler(event -> fireChange());
            }

            @Override
            protected void onDisconnect() {
                super.onDisconnect();
                handlerRegistration.removeHandler();
                handlerRegistration = null;
            }
        };
    }
}
