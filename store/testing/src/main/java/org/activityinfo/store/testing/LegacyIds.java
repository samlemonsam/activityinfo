package org.activityinfo.store.testing;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;

/**
 * Generates ids that are compatible with the current
 * MySql layout
 */
public class LegacyIds implements Ids {

    private final KeyGenerator keyGenerator = new KeyGenerator();

    private final int databaseId;

    public LegacyIds(int databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public int databaseId() {
        return databaseId;
    }

    @Override
    public ResourceId formId(String defaultName) {
        return CuidAdapter.activityFormClass(keyGenerator.generateInt());
    }

    @Override
    public ResourceId enumFieldId(String defaultName) {
        return CuidAdapter.attributeId(keyGenerator.generateInt());
    }

    @Override
    public ResourceId fieldId(String defaultName) {
        return CuidAdapter.indicatorField(keyGenerator.generateInt());
    }

    @Override
    public ResourceId recordId(ResourceId formId, int index) {
        return CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, keyGenerator.generateInt());
    }

    @Override
    public EnumItem enumItem(String label) {
        return new EnumItem(CuidAdapter.attributeId(keyGenerator.generateInt()), label);
    }
}
