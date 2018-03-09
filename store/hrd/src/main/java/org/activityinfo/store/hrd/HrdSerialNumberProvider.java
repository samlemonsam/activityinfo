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

import com.google.appengine.api.datastore.*;
import com.google.common.base.Strings;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.SerialNumberProvider;


public class HrdSerialNumberProvider implements SerialNumberProvider {

    public static final String KIND = "SerialNumber";
    public static final String PROPERTY = "Serial";

    private DatastoreService datastore;

    public HrdSerialNumberProvider() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @Override
    public int next(ResourceId formId, ResourceId fieldId, String prefix) {

        StringBuilder keyString = new StringBuilder();
        keyString.append(formId.asString());
        keyString.append('\0');
        keyString.append(fieldId.asString());

        if(!Strings.isNullOrEmpty(prefix)) {
            keyString.append('\0');
            keyString.append(prefix);
        }

        Key key = KeyFactory.createKey(KIND, keyString.toString());

        Transaction tx = datastore.beginTransaction();

        Entity entity;
        int number;
        try {
            entity = datastore.get(tx, key);
            number = ((Number)entity.getProperty(PROPERTY)).intValue();
            number = number + 1;

        } catch (EntityNotFoundException e) {
            entity = new Entity(key);
            number = 1;
        }

        entity.setProperty(PROPERTY, number);

        datastore.put(tx, entity);
        tx.commit();

        return number;
    }
}
