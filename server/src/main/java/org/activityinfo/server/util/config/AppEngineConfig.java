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
package org.activityinfo.server.util.config;

import com.google.appengine.api.datastore.*;

/**
 * Utility class for retrieving / storing properties files from the AppEngine
 * datastore.
 * <p/>
 * <p/>
 * We just store the text of the configuration file to a single key in the
 * datastore.
 */
public class AppEngineConfig {

    public static String getPropertyFile() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity;
        try {
            entity = datastore.get(key());
        } catch (EntityNotFoundException e) {
            return "";
        }
        Text text = (Text) entity.getProperty("text");
        return text.getValue();
    }

    public static void setPropertyFile(String string) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity = new Entity(key());
        entity.setProperty("text", new Text(string));
        datastore.put(entity);
    }

    public static Key key() {
        return KeyFactory.createKey("Configuration", "config");
    }

}
