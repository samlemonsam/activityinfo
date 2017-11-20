package org.activityinfo.test.api;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.store.testing.Ids;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates ids that are compatible with the current
 * MySql layout for sites
 */
public class SiteIds implements Ids {

    private final KeyGenerator keyGenerator = new KeyGenerator();

    private TestDatabase database;
    private final int activityId;

    public SiteIds(TestDatabase database) {
        this.database = database;
        this.activityId = keyGenerator.generateInt();
    }

    @Override
    public int databaseId() {
        return database.getId();
    }

    @Override
    public ResourceId formId(String defaultName) {
        return CuidAdapter.activityFormClass(activityId);
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

    @Override
    public List<FormField> builtinFields() {
        FormField partnerField = new FormField(CuidAdapter.partnerField(activityId));
        partnerField.setRequired(true);
        partnerField.setLabel("Partner");
        partnerField.setType(new ReferenceType(Cardinality.SINGLE, CuidAdapter.partnerFormId(database.getId())));

        return Collections.singletonList(partnerField);
    }

    @Override
    public Map<ResourceId, FieldValue> builtinValues() {
        Map<ResourceId, FieldValue> valueMap = new HashMap<>();
        valueMap.put(CuidAdapter.partnerField(activityId), database.getDefaultPartner());

        return valueMap;
    }


}
