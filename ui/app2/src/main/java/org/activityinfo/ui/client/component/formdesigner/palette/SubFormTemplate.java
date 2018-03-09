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

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;

/**
 * @author yuriyz on 01/21/2015.
 */
public class SubFormTemplate implements Template<FormField> {

    private final String label;
    private final SubFormKind kind;

    public SubFormTemplate(String label, SubFormKind kind) {
        this.label = label;
        this.kind = kind;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public FormField create() {
        FormField field = new FormField(ResourceId.generateFieldId(SubFormReferenceType.TYPE_CLASS));
        field.setLabel(label);
        field.setType(new SubFormReferenceType());
        return field;
    }

    public SubFormKind getKind() {
        return kind;
    }
}
