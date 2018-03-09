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
package org.activityinfo.ui.client.component.formdesigner.palette;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.List;

public class CheckboxTemplate implements FieldTemplate {
    @Override
    public String getLabel() {
        return I18N.CONSTANTS.multipleSelection();
    }

    @Override
    public FormField create() {
        List<EnumItem> values = Lists.newArrayList();
        values.add(new EnumItem(EnumItem.generateId(), I18N.MESSAGES.defaultEnumItem(1)));
        values.add(new EnumItem(EnumItem.generateId(), I18N.MESSAGES.defaultEnumItem(2)));
        FormField field = new FormField(ResourceId.generateFieldId(EnumType.TYPE_CLASS));
        field.setLabel(I18N.CONSTANTS.defaultCheckboxFieldLabel());
        field.setType(new EnumType(Cardinality.MULTIPLE, values));

        return field;
    }
}
