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

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.columns.*;
import org.activityinfo.store.hrd.entity.*;
import org.activityinfo.store.query.server.InvalidUpdateException;
import org.activityinfo.store.spi.RecordChangeType;
import org.activityinfo.store.spi.TypedRecordUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        // Queue up batch
        List<Object> toSave = new ArrayList<>();
        List<Key> toDelete = new ArrayList<>();

        // Update column-based storage, if active
        if(rootEntity.getActiveColumnStorage() != null) {

            FormColumnStorage columnStorage = ofy().load().key(FormColumnStorage.key(rootEntity)).safe();
            columnStorage.setVersion(newVersion);

            if(changeType == RecordChangeType.CREATED) {
                int newRecordCount = columnStorage.getRecordCount() + 1;
                int newRecordIndex = newRecordCount;
                updated.setRecordNumber(columnStorage.getScheme(), newRecordIndex);
                columnStorage.setRecordCount(newRecordCount);

                toSave.add(columnStorage);
                toSave.add(updateRecordIdBlock(updated, newRecordIndex));

            } else if(changeType == RecordChangeType.DELETED) {
                columnStorage.setDeletedCount(columnStorage.getDeletedCount() + 1);
                toSave.add(updateTombstone(existingEntity.getRecordNumber(columnStorage.getScheme())));
                toSave.add(columnStorage);
            }

            if(changeType != RecordChangeType.DELETED) {
                int recordIndex = updated.getRecordNumber(columnStorage.getScheme());
                updateColumnBlocks(formClass, update, recordIndex, toSave);
            }
        }

        // Update record-based storage
        toSave.add(rootEntity);
        toSave.add(snapshotEntity);

        if (update.isDeleted()) {
            toDelete.add(Key.create(updated));
        } else {
            toSave.add(updated);
        }

        ofy().save().entities(toSave);
        if(!toDelete.isEmpty()) {
            ofy().delete().entities(toDelete);
        }
    }


    private void updateColumnBlocks(FormClass formSchema,
                                    TypedRecordUpdate update,
                                    int recordIndex, List<Object> toSave) {

        Map<ResourceId, FieldValue> changed = update.getChangedFieldValues();
        for (FormField field : formSchema.getFields()) {
            if (changed.containsKey(field.getId())) {
                Entity updatedBlock = updateBlock(recordIndex, field, changed.get(field.getId()));
                if(updatedBlock != null) {
                    toSave.add(updatedBlock);
                }
            }
        }
    }

    private Entity updateRecordIdBlock(FormRecordEntity updated, int recordIndex) {
        BlockManager blockManager = new RecordIdBlock();
        BlockDescriptor descriptor = blockManager.getBlockDescriptor(formId, RecordIdBlock.FIELD_NAME, recordIndex);

        return doUpdate(blockManager, descriptor, recordIndex, TextValue.valueOf(updated.getRecordId().asString()));
    }

    private Entity updateBlock(int recordIndex, FormField field, FieldValue fieldValue) {

        BlockManager blockManager = BlockFactory.get(field.getType());
        BlockDescriptor descriptor = blockManager.getBlockDescriptor(formId, field.getName(), recordIndex);

        return doUpdate(blockManager, descriptor, recordIndex, fieldValue);
    }

    private Entity doUpdate(BlockManager blockManager, BlockDescriptor descriptor, int recordIndex, FieldValue fieldValue) {
        com.google.appengine.api.datastore.Key blockKey = descriptor.key();

        Entity blockEntity;
        try {
            blockEntity = DatastoreServiceFactory.getDatastoreService().get(ofy().getTransaction(), blockKey);
        } catch (EntityNotFoundException e) {
            blockEntity = null;
        }
        if(blockEntity == null) {
            blockEntity = new Entity(descriptor.key());
        }
        return blockManager.update(blockEntity, descriptor.getOffset(recordIndex), fieldValue);
    }

    private Entity updateTombstone(int recordIndex) {
        TombstoneBlock tombstone = new TombstoneBlock();
        BlockDescriptor descriptor = tombstone.getBlockDescriptor(formId, recordIndex);
        Entity blockEntity;
        try {
            blockEntity = DatastoreServiceFactory.getDatastoreService().get(ofy().getTransaction(), descriptor.key());
        } catch (EntityNotFoundException e) {
            blockEntity = new Entity(descriptor.key());
        }

        tombstone.markDeleted(blockEntity, descriptor.getOffset(recordIndex));

        return blockEntity;
    }
}
