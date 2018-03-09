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
package org.activityinfo.model.resource;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;

import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.json.Json.toJson;

/**
 *  Constructs a set of updates to be applied atomically
 */
public class RecordTransactionBuilder {

    private List<RecordUpdate> updates = new ArrayList<>();

    public Iterable<RecordUpdate> getUpdates() {
        return updates;
    }



    public RecordUpdate create(ResourceId formId, ResourceId resourceId)  {
        RecordUpdate update = new RecordUpdate();
        update.setRecordId(resourceId);
        update.setFormId(formId);
        updates.add(update);
        return update;
    }


    public RecordTransactionBuilder create(FormInstance record) {
        RecordUpdate update = new RecordUpdate();
        update.setFormId(record.getFormId());
        update.setRecordId(record.getId());
        update.setFields(FormRecord.fromInstance(record).getFields());
        updates.add(update);
        return this;
    }

    public RecordTransactionBuilder add(RecordUpdate update) {
        updates.add(update);
        return this;
    }

    public RecordTransactionBuilder add(Iterable<RecordUpdate> updates) {
        for (RecordUpdate update : updates) {
            add(update);
        }
        return this;
    }

    public RecordTransactionBuilder delete(ResourceId formId, ResourceId id) {
        RecordUpdate update = new RecordUpdate();
        update.setFormId(formId);
        update.setRecordId(id);
        update.delete();
        add(update);
        return this;
    }

    public RecordUpdate update(ResourceId formId, ResourceId id) {
        RecordUpdate update = new RecordUpdate();
        update.setFormId(formId);
        update.setRecordId(id);
        updates.add(update);
        return update;
    }

    public RecordTransaction build() {
        RecordTransaction tx = new RecordTransaction();
        tx.changes = this.updates.toArray(new RecordUpdate[this.updates.size()]);
        return tx;
    }

    public JsonValue toJsonObject() {
        return toJson(build());
    }

}
