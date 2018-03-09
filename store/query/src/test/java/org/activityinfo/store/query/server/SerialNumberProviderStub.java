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
package org.activityinfo.store.query.server;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.SerialNumberProvider;

import java.util.HashMap;
import java.util.Map;

public class SerialNumberProviderStub implements SerialNumberProvider {

    private Map<String, Integer> nextSerialNumber = new HashMap<>();

    @Override
    public int next(ResourceId formId, ResourceId fieldId, String prefix) {
        String key = formId.asString() + "-" + fieldId.asString() + "-" + prefix;
        Integer next = nextSerialNumber.get(key);
        if(next == null) {
            next = 1;
        }
        nextSerialNumber.put(key, next + 1);
        return next;
    }
}
