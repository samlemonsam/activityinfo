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
package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.gwt.core.shared.GwtIncompatible;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.*;

import java.util.*;
import java.util.function.Predicate;

@GwtIncompatible
public class TestingFormStorage implements VersionedFormStorage {

    private long version = 1;

    private TestForm testForm;

    private Map<String, Integer> serialNumbers = new HashMap<>();

    private List<FormInstance> records = null;

    private Map<ResourceId, FormInstance> index = null;

    public TestingFormStorage(TestForm testForm) {
        this.testForm = testForm;
    }

    public Supplier<FormInstance> getGenerator() {
        return testForm.getGenerator();
    }

    private List<FormInstance> records() {
        if(records == null) {
            return testForm.getRecords();
        }
        return records;
    }

    private void ensureWeHaveOwnCopy() {
        if(records == null) {
            records = new ArrayList<>();
            for (FormInstance record : testForm.getRecords()) {
                records.add(record.copy());
            }
            index = new HashMap<>();
            for (FormInstance record : records) {
                index.put(record.getId(), record);
            }
        }
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        ensureWeHaveOwnCopy();

        FormInstance instance = index.get(resourceId);
        if(instance == null) {
            return Optional.absent();
        } else {
            return Optional.of(FormRecord.fromInstance(instance));
        }
    }

    @Override
    public List<FormRecord> getSubRecords(ResourceId parentId) {

        List<FormRecord> result = new ArrayList<>();
        for (FormInstance record : records()) {
            if(parentId.equals(record.getParentRecordId())) {
                result.add(FormRecord.fromInstance(record));
            }
        }
        return result;
    }

    @Override
    public List<RecordVersion> getVersions(ResourceId recordId) {
        return Collections.emptyList();
    }

    @Override
    public List<RecordVersion> getVersionsForParent(ResourceId parentRecordId) {
        return Collections.emptyList();
    }

    @Override
    public FormSyncSet getVersionRange(long localVersion, long toVersion, Predicate<ResourceId> visibilityPredicate, java.util.Optional<String> cursor) {
        List<FormRecord> records = new ArrayList<>();
        if(localVersion < version) {
            for (FormInstance record : records()) {
                records.add(FormRecord.fromInstance(record));
            }
        }
        return FormSyncSet.initial(testForm.getFormId(), records, java.util.Optional.empty());
    }

    @Override
    public FormClass getFormClass() {
        return SubFormPatch.patch(testForm.getFormClass());
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(TypedRecordUpdate update) {

        FormInstance newRecord = new FormInstance(update.getRecordId(), update.getFormId());
        for (Map.Entry<ResourceId, FieldValue> entry : update.getChangedFieldValues().entrySet()) {
            newRecord.set(entry.getKey(), entry.getValue());
        }

        ensureWeHaveOwnCopy();
        records.add(newRecord);
        index.put(newRecord.getId(), newRecord);
        version++;
    }


    @Override
    public void update(TypedRecordUpdate update) {
        ensureWeHaveOwnCopy();
        if(update.isDeleted()) {
            FormInstance deleted = index.remove(update.getRecordId());
            records.remove(deleted);
        } else if(!index.containsKey(update.getRecordId())) {
            // Create
            FormInstance newRecord = new FormInstance(update.getFormId(), update.getFormId());
            newRecord.setParentRecordId(update.getParentId());
            newRecord.setAll(update.getChangedFieldValues());
            records.add(newRecord);

        } else {
            // Update
            throw new UnsupportedOperationException();
        }
        version++;
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new TestingFormQueryBuilder(getFormClass(), records());
    }

    @Override
    public long cacheVersion() {
        return version;
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException();
    }

    public Integer nextSerialNumber(ResourceId fieldId, String prefix) {
        Integer nextNumber = serialNumbers.get(prefix);
        if(nextNumber == null) {
            nextNumber = records().size() + 1;
        }

        serialNumbers.put(prefix, nextNumber + 1);

        return nextNumber;

    }
}
