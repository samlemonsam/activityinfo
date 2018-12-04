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
package org.activityinfo.server.util.locale;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.inject.Provider;
import net.lightoze.gwt.i18n.client.LocaleFactory;


public class LocalizableResourceProvider<T extends LocalizableResource> implements Provider<T> {

    private Class<T> clazz;

    public LocalizableResourceProvider(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public T get() {
        return LocaleFactory.get(clazz);
    }
}
