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
package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.FormStorage;

import java.io.IOException;
import java.util.*;

public class FormStorageProviderStub implements FormStorageProvider {
    
    private Map<ResourceId, JsonFormStorage> map = new HashMap<>();
    
    
    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {
        return Optional.<FormStorage>fromNullable(map.get(formId));
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId formId) {
        return getForm(formId).get().getFormClass();
    }
    
    public void addJsonCollection(String resourceName) throws IOException {
        add(new JsonFormStorage(resourceName));
    }
    
    public void add(JsonFormStorage accessor) {
        map.put(accessor.getFormClass().getId(), accessor);
    }

    public boolean contains(ResourceId resourceId) {
        return map.containsKey(resourceId);
    }
    
}
