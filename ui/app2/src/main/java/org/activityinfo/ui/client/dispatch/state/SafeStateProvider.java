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
package org.activityinfo.ui.client.dispatch.state;

import com.extjs.gxt.ui.client.state.Provider;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GXT state provider that either takes advantage of the HTML5 LocalStorage or
 * saves stage to an in-memory HashMap.
 * <p/>
 * This is used in place of the default CookieProvider because with TreePanels
 * the amount of state stored in cookies can quickly explode.
 */
@Singleton
public final class SafeStateProvider extends Provider {

    private static final Logger LOGGER = Logger.getLogger(SafeStateProvider.class.getName());

    private Map<String, String> stateMap = new HashMap<String, String>();

    public SafeStateProvider() {
        if (Storage.isLocalStorageSupported()) {
            stateMap = new StorageMap(Storage.getLocalStorageIfSupported());
        } else {
            stateMap = new HashMap<String, String>();
        }
    }

    @Override
    protected void setValue(String name, String value) {
        try {
            stateMap.put(name, value);
        } catch(Exception e) {
            LOGGER.log(Level.WARNING, "setValue() failed", e);
        }
    }

    @Override
    protected String getValue(String name) {
        try {
            return stateMap.get(name);
        } catch(Exception e) {
            LOGGER.log(Level.WARNING, "getValue() failed", e);
            return null;
        }
    }

    @Override
    protected void clearKey(String name) {
        stateMap.remove(name);
    }
}