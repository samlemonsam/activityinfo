package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.FormPermissions;
import org.activityinfo.service.store.FormStorage;
import org.activityinfo.service.store.RecordVersion;

import java.util.Collections;
import java.util.List;


public class TestingFormStorage implements FormStorage {

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
        return new TestingFormQueryBuilder();
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
