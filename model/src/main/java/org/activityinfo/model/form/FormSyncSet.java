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
package org.activityinfo.model.form;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

/**
 * Set of updates to apply to a local copy of the database
 */
@JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
public final class FormSyncSet {

    private String formId;
    private boolean reset;
    private String[] deleted;
    private UpdatedRecord[] updatedRecords;

    public FormSyncSet() {
    }

    @JsOverlay
    public static FormSyncSet incremental(String formId, String[] deleted, List<FormRecord> records) {
        FormSyncSet syncSet = new FormSyncSet();
        syncSet.formId = formId;
        syncSet.reset = false;
        syncSet.deleted = deleted;
        syncSet.updatedRecords = new UpdatedRecord[records.size()];
        for (int i = 0; i < syncSet.updatedRecords.length; i++) {
            syncSet.updatedRecords[i] = UpdatedRecord.create(records.get(i));
        }
        return syncSet;
    }

    @JsOverlay
    public static FormSyncSet complete(ResourceId formId, List<FormRecord> records) {
        FormSyncSet syncSet = new FormSyncSet();
        syncSet.formId = formId.asString();
        syncSet.reset = true;
        syncSet.deleted = new String[0];
        syncSet.updatedRecords = new UpdatedRecord[records.size()];
        for (int i = 0; i < syncSet.updatedRecords.length; i++) {
            syncSet.updatedRecords[i] = UpdatedRecord.create(records.get(i));
        }
        return syncSet;
    }

    @JsOverlay
    public static FormSyncSet emptySet(ResourceId formId) {
        FormSyncSet syncSet = new FormSyncSet();
        syncSet.formId = formId.asString();
        syncSet.deleted = new String[0];
        syncSet.updatedRecords = new UpdatedRecord[0];
        return syncSet;
    }

    @JsOverlay
    public String getFormId() {
        return formId;
    }

    @JsOverlay
    public Iterable<String> getDeleted() {
        return Arrays.asList(deleted);
    }

    @JsOverlay
    public Iterable<UpdatedRecord> getUpdatedRecords() {
        return Arrays.asList(updatedRecords);
    }

    @JsOverlay
    public int getUpdatedRecordCount() {
        return updatedRecords.length;
    }

    @JsOverlay
    public boolean isEmpty() {
        return updatedRecords.length == 0 && deleted.length == 0;
    }

    @JsOverlay
    public UpdatedRecord getUpdatedRecord(int i) {
        return updatedRecords[i];
    }

    @JsOverlay
    public boolean isReset() {
        return reset;
    }

}
