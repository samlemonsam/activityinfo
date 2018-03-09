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
package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.ApplicationProperties;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

/**
 * Represents a level within a hierarchy.
 */
public class Level {

    private FormClass formClass;

    ResourceId parentFormId;
    ResourceId parentFieldId;
    ResourceId labelFieldId;

    Level parent;
    List<Level> children = Lists.newArrayList();

    Level(FormClass formClass) {
        this.formClass = formClass;
        for(FormField field : formClass.getFields()) {
            if(field.isSubPropertyOf(ApplicationProperties.PARENT_PROPERTY)) {
                ReferenceType type = (ReferenceType) field.getType();
                assert type.getRange().size() == 1;
                parentFormId = type.getRange().iterator().next();
                parentFieldId = field.getId();
            } else if(field.getType() instanceof TextType) {
                // use first text field as label field
                if(this.labelFieldId == null) {
                    this.labelFieldId = field.getId();
                }
            }
        }
    }

    public ResourceId getFormId() {
        return formClass.getId();
    }

    public String getLabel() {
        return formClass.getLabel();
    }

    public FormClass getFormClass() {
        return formClass;
    }

    public Level getParent() {
        return parent;
    }

    public ResourceId getParentFieldId() {
        return parentFieldId;
    }

    public ResourceId getLabelFieldId() {
        return labelFieldId;
    }

    public boolean isRoot() {
        return parentFormId == null;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public List<Level> getChildren() {
        return children;
    }


    public Choice toChoice(FormInstance instance) {


        if(isRoot()) {
            return new Choice(getFormId(), instance.getId(), instance.getString(labelFieldId));

        } else {
            ReferenceValue parentRefValue = (ReferenceValue) instance.get(parentFieldId);
            RecordRef parentRef = parentRefValue.getOnlyReference();

            return new Choice(getFormId(), instance.getId(), instance.getString(labelFieldId), parentRef);
        }
    }
}
