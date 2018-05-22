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
import java.util.Optional;

/**
 * Set of updates to apply to a local copy of the database
 */
@JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
public final class FormSyncSet {

    private String formId;
    private boolean reset;
    private String[] deleted;
    private UpdatedRecord[] updatedRecords;
    private String cursor;

    public FormSyncSet() {
    }

    @JsOverlay
    public static FormSyncSet incremental(String formId, String[] deleted, List<FormRecord> records, Optional<String> cursor) {
        FormSyncSet syncSet = new FormSyncSet();
        syncSet.formId = formId;
        syncSet.reset = false;
        syncSet.deleted = deleted;
        syncSet.updatedRecords = new UpdatedRecord[records.size()];
        syncSet.cursor = cursor.orElse(null);
        for (int i = 0; i < syncSet.updatedRecords.length; i++) {
            syncSet.updatedRecords[i] = UpdatedRecord.create(records.get(i));
        }
        return syncSet;
    }

    @JsOverlay
    public static FormSyncSet initial(ResourceId formId, List<FormRecord> records, Optional<String> cursor) {
        FormSyncSet syncSet = new FormSyncSet();
        syncSet.formId = formId.asString();
        syncSet.reset = true;
        syncSet.deleted = new String[0];
        syncSet.updatedRecords = new UpdatedRecord[records.size()];
        syncSet.cursor = cursor.orElse(null);
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
    public List<String> getDeleted() {
        return Arrays.asList(deleted);
    }

    @JsOverlay
    public List<UpdatedRecord> getUpdatedRecords() {
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

    /**
     * @return true if there are no remaining chunks to retrieve.
     */
    @JsOverlay
    public boolean isComplete() {
        return cursor == null;
    }

    @JsOverlay
    public String getCursor() {
        return cursor;
    }

    @JsOverlay
    public boolean isReset() {
        return reset;
    }

    @JsOverlay
    public static FormSyncSet foldLeft(FormSyncSet a, FormSyncSet b) {
        assert a.formId.equals(b.formId);

        FormSyncSet merged = new FormSyncSet();
        merged.formId = a.formId;
        merged.reset = a.reset;
        merged.deleted = combine(a.deleted, b.deleted);
        merged.updatedRecords = combine(a.updatedRecords, b.updatedRecords);
        merged.cursor = b.cursor;
        return merged;
    }

    @JsOverlay
    private static String[] combine(String[] a, String[] b) {
        if(a.length == 0) {
            return b;
        }
        if(b.length == 0) {
            return a;
        }
        String[] c = new String[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    @JsOverlay
    private static UpdatedRecord[] combine(UpdatedRecord[] a, UpdatedRecord[] b) {
        if(a.length == 0) {
            return b;
        }
        if(b.length == 0) {
            return a;
        }
        UpdatedRecord[] c = new UpdatedRecord[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
