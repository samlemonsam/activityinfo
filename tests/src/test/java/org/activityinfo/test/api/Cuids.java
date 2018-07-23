package org.activityinfo.test.api;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.store.testing.Ids;
import org.activityinfo.store.testing.UnitTestingIds;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Cuids implements Ids {

    private TestDatabase testDatabase;

    public Cuids(TestDatabase testDatabase) {
        this.testDatabase = testDatabase;
    }

    @Override
    public int databaseId() {
        return testDatabase.getId();
    }

    @Override
    public ResourceId formId(String defaultName) {
        return ResourceId.generateId();
    }

    @Override
    public ResourceId enumFieldId(String defaultName) {
        return ResourceId.generateId();
    }

    @Override
    public ResourceId fieldId(String defaultName) {
        return ResourceId.valueOf(defaultName);
    }

    @Override
    public ResourceId recordId(ResourceId formId, int index) {
        return ResourceId.generateId();
    }

    @Override
    public EnumItem enumItem(String label) {
        return new EnumItem(ResourceId.valueOf(UnitTestingIds.makeId(label)), label);
    }

    @Override
    public List<FormField> builtinFields() {
        return Collections.emptyList();
    }

    @Override
    public Map<ResourceId, FieldValue> builtinValues() {
        return Collections.emptyMap();
    }
}
