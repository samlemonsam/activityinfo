package org.activityinfo.store.testing;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;

/**
 * Creates friendly, human readable ids for unit tests.
 */
public class UnitTestingIds implements Ids {
    @Override
    public int databaseId() {
        return 1;
    }

    @Override
    public ResourceId formId(String defaultName) {
        return ResourceId.valueOf(defaultName);
    }

    @Override
    public ResourceId enumFieldId(String defaultName) {
        return ResourceId.valueOf(defaultName);
    }

    @Override
    public ResourceId fieldId(String defaultName) {
        return ResourceId.valueOf(defaultName);
    }

    @Override
    public ResourceId recordId(ResourceId formId, int index) {
        return ResourceId.valueOf("c" + index);
    }

    @Override
    public EnumItem enumItem(String label) {
        return new EnumItem(ResourceId.valueOf(makeId(label)), label);
    }

    private String makeId(String label) {
        return label.toUpperCase().replace(" ", "_");
    }
}
