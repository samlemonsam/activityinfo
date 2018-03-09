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
package org.activityinfo.test.api;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceType;
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
