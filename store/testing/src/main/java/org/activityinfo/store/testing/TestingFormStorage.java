package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.*;

import java.util.*;


public class TestingFormStorage implements VersionedFormStorage {

    private TestForm testForm;

    private Map<String, Integer> serialNumbers = new HashMap<>();

    private List<FormInstance> records = null;

    private Map<ResourceId, FormInstance> index = null;

    public TestingFormStorage(TestForm testForm) {
        this.testForm = testForm;
    }

    public RecordGenerator getGenerator() {
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
    public FormPermissions getPermissions(int userId) {
        return FormPermissions.full();
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
    public List<RecordVersion> getVersions(ResourceId recordId) {
        return Collections.emptyList();
    }

    @Override
    public List<RecordVersion> getVersionsForParent(ResourceId parentRecordId) {
        return Collections.emptyList();
    }

    @Override
    public List<FormRecord> getVersionRange(long localVersion, long toVersion) {
        List<FormRecord> records = new ArrayList<>();
        if(localVersion < 1) {
            for (FormInstance record : records()) {
                records.add(FormRecord.fromInstance(record));
            }
        }
        return records;
    }

    @Override
    public FormClass getFormClass() {
        return testForm.getFormClass();
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(RecordUpdate update) {

        FormInstance newRecord = new FormInstance(update.getRecordId(), update.getFormId());
        for (Map.Entry<ResourceId, FieldValue> entry : update.getChangedFieldValues().entrySet()) {
            newRecord.set(entry.getKey(), entry.getValue());
        }

        ensureWeHaveOwnCopy();
        records.add(newRecord);
        index.put(newRecord.getId(), newRecord);
    }


    @Override
    public void update(RecordUpdate update) {
        ensureWeHaveOwnCopy();
        if(update.isDeleted()) {
            FormInstance deleted = index.remove(update.getRecordId());
            records.remove(deleted);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new TestingFormQueryBuilder(getFormClass(), records());
    }

    @Override
    public long cacheVersion() {
        return 1;
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
