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

import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.query.server.InvalidUpdateException;
import org.activityinfo.store.spi.RecordChangeType;
import org.activityinfo.store.spi.TypedRecordUpdate;

import static com.googlecode.objectify.ObjectifyService.ofy;


public class CreateOrUpdateRecord extends VoidWork {

    private ResourceId formId;
    private TypedRecordUpdate update;

    public CreateOrUpdateRecord(ResourceId formId, TypedRecordUpdate update) {
        this.formId = formId;
        this.update = update;
    }


    @Override
    public void vrun() {

        FormEntity rootEntity = ofy().load().key(FormEntity.key(formId)).safe();
        FormClass formClass = ofy().load().key(FormSchemaEntity.key(formId)).safe().readFormClass();

        long currentVersion = rootEntity.getVersion();
        long newVersion = currentVersion + 1;

        rootEntity.setVersion(newVersion);

        FormRecordEntity existingEntity = ofy().load().key(FormRecordEntity.key(formClass, update.getRecordId())).now();
        FormRecordEntity updated;
        RecordChangeType changeType;
        
        if(existingEntity != null) {
            updated = existingEntity;
            changeType = update.isDeleted() ? RecordChangeType.DELETED : RecordChangeType.UPDATED;

        } else {
            updated = new FormRecordEntity(formId, update.getRecordId());
            changeType = RecordChangeType.CREATED;

            if (update.isDeleted()) {
                throw new InvalidUpdateException("Creation of entity with deleted flag is not allowed.");
            }

            if (formClass.getParentFormId().isPresent()) {
                ResourceId parentId = update.getParentId();
                if (parentId == null) {
                    throw new InvalidUpdateException("@parent is required for subform submissions");
                }
                updated.setParentRecordId(parentId);
            }
        }
        updated.setVersion(newVersion);
        updated.setSchemaVersion(rootEntity.getSchemaVersion());
        updated.setFieldValues(formClass, update.getChangedFieldValues());
        
        // Store a copy as a snapshot
        FormRecordSnapshotEntity snapshotEntity = new FormRecordSnapshotEntity(update.getUserId(), changeType, updated);

        if (update.isDeleted()) {
            ofy().save().entities(rootEntity, snapshotEntity);
            ofy().delete().entities(updated);
        } else {
            ofy().save().entities(rootEntity, updated, snapshotEntity);
        }

    }

}
