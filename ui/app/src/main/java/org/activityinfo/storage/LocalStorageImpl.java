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
package org.activityinfo.storage;

import com.google.common.base.Optional;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public final class LocalStorageImpl implements LocalStorage {

    private static final Logger LOGGER = Logger.getLogger(LocalStorageImpl.class.getName());

    public LocalStorageImpl() {
    }

    @Override
    public void setObject(String keyName, JsonValue jsonValue) {
        setItem(keyName, jsonValue.toJson());
    }

    public native void setItem(String keyName, String json) /*-{
        $wnd.localStorage.setItem(keyName, json);
    }-*/;

    @Nullable
    @Override
    public Optional<JsonValue> getObjectIfPresent(String keyName) {
        String jsonString = getItem(keyName);
        if(jsonString != null) {
            try {
                return Optional.of(Json.parse(jsonString));
            } catch (Exception e) {
                LOGGER.warning("Exception parsing key " + keyName);
            }
        }

        return Optional.absent();
    }

    public native String getItem(String keyName) /*-{
        return $wnd.localStorage.getItem(keyName);
    }-*/;
}
