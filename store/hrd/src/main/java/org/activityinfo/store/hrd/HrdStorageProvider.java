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

import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.hrd.op.CreateOrUpdateForm;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.FormStorage;

import java.util.*;

/**
 * Catalog of Collection hosted in the AppEngine High Replication Datastore
 */
public class HrdStorageProvider implements FormStorageProvider {


    public HrdFormStorage create(FormClass formClass) {
        Hrd.ofy().transact(new CreateOrUpdateForm(formClass));
        
        return new HrdFormStorage(formClass);
    }
    
    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {

        FormSchemaEntity schemaEntity = Hrd.ofy().load().key(FormSchemaEntity.key(formId)).now();
        if(schemaEntity == null) {
            return Optional.absent();
        }

        HrdFormStorage accessor = new HrdFormStorage(schemaEntity.readFormClass());
        
        return Optional.<FormStorage>of(accessor);
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        
        Set<Key<FormSchemaEntity>> toLoad = new HashSet<>();
        for (ResourceId formId : formIds) {
            toLoad.add(FormSchemaEntity.key(formId));
        }
        Map<Key<FormSchemaEntity>, FormSchemaEntity> entityMap = ObjectifyService.ofy().load().keys(toLoad);
        
        Map<ResourceId, FormClass> formClassMap = new HashMap<>();
        for (FormSchemaEntity formSchema : entityMap.values()) {
            formClassMap.put(formSchema.getFormId(), formSchema.readFormClass());
        }
        
        return formClassMap;
    }

    @Override
    public FormClass getFormClass(ResourceId formId) {
        return getForm(formId).get().getFormClass();
    }
}
