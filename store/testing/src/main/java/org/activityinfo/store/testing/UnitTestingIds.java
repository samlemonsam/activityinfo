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

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<FormField> builtinFields() {
        return Collections.emptyList();
    }

    @Override
    public Map<ResourceId, FieldValue> builtinValues() {
        return Collections.emptyMap();
    }

    public static String makeId(String label) {
        return label.toUpperCase().replace(" ", "_");
    }
}
