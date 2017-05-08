package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TestingFormStorage implements VersionedFormStorage {

    private TestForm testForm;

    public TestingFormStorage(TestForm testForm) {
        this.testForm = testForm;
    }

    @Override
    public FormPermissions getPermissions(int userId) {
        return FormPermissions.readonly();
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        return Optional.absent();
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
            for (FormInstance record : testForm.getRecords()) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(RecordUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new TestingFormQueryBuilder(testForm.getRecords());
    }

    @Override
    public long cacheVersion() {
        return 1;
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException();
    }
}
