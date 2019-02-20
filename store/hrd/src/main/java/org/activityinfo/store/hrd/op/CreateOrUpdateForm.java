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
package org.activityinfo.store.hrd.op;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.store.hrd.HrdFormStorage;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;

import static com.googlecode.objectify.ObjectifyService.ofy;


public class CreateOrUpdateForm implements Work<HrdFormStorage> {
    
    private FormClass formClass;

    public CreateOrUpdateForm(FormClass formClass) {
        this.formClass = formClass;
    }
    
    @Override
    public HrdFormStorage run() {

        Key<FormSchemaEntity> schemaKey = FormSchemaEntity.key(formClass.getId());
        FormSchemaEntity schemaEntity = ofy().load().key(schemaKey).now();
        
        if(schemaEntity == null) {
            return create();
        } else {
            return update(schemaEntity);
        }
    }

    private HrdFormStorage create() {
        FormEntity rootEntity = new FormEntity();
        rootEntity.setId(formClass.getId());
        rootEntity.setVersion(1);
        rootEntity.setSchemaVersion(1);
        rootEntity.setColumnStorageActive(true);

        FormSchemaEntity formClassEntity = new FormSchemaEntity(formClass);
        formClassEntity.setSchemaVersion(1);

        ofy().save().entities(rootEntity, formClassEntity);

        return new HrdFormStorage(rootEntity, formClass);
    }

    private HrdFormStorage update(FormSchemaEntity formClassEntity) {

        FormEntity rootEntity = ofy().load().key(FormEntity.key(formClass)).safe();
        
        // Increment the version counter
        long newVersion = rootEntity.getVersion() + 1;
        rootEntity.setVersion(newVersion);
        rootEntity.setSchemaVersion(newVersion);
        
        // Update the schema
        formClassEntity.setSchema(formClass);
        formClassEntity.setSchemaVersion(newVersion);
        
        ofy().save().entities(rootEntity, formClassEntity);

        return new HrdFormStorage(rootEntity, formClass);
    }
}
