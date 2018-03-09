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
package org.activityinfo.indexedb;

import org.activityinfo.json.JsonValue;

public interface KeyPath {

    ObjectKey buildKey(JsonValue value);

    public static KeyPath from(ObjectStoreOptions options) {
        if(options.getKeyPath() instanceof String) {
            String keyName = (String) options.getKeyPath();
            return jsonObject -> new ObjectKey(jsonObject.getString(keyName));
        }

        if(options.getKeyPath() instanceof String[]) {
            String keyNames[] = (String[]) options.getKeyPath();
            return jsonObject -> {
                String[] key = new String[keyNames.length];
                for (int i = 0; i < key.length; i++) {
                    key[i] = jsonObject.getString(keyNames[i]);
                }
                return new ObjectKey(key);
            };
        }

        return jsonObject -> {
            throw new UnsupportedOperationException("No key path provided");
        };
    }
}
