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
package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.CursorObserver;

public class FieldObserver {
    
    private final String name;
    private final FieldConverter<?> converter;
    private final CursorObserver<FieldValue> observer;

    public FieldObserver(String name, FieldConverter<?> converter, CursorObserver<FieldValue> observer) {
        this.name = name;
        this.converter = converter;
        this.observer = observer;
    }

    public void onNext(EmbeddedEntity entity) {
        Object value = entity.getProperty(name);
        if(value == null) {
            observer.onNext(null);
        } else {
            observer.onNext(converter.toFieldValue(value));
        }
    }
}
