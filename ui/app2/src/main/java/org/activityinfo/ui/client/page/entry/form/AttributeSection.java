package org.activityinfo.ui.client.page.entry.form;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.ui.client.page.entry.form.field.AttributeCheckBoxGroup;
import org.activityinfo.ui.client.page.entry.form.field.AttributeCombo;
import org.activityinfo.ui.client.page.entry.form.field.AttributeField;

import java.util.List;

public class AttributeSection extends FormSectionWithFormLayout<SiteDTO> {

    private List<AttributeField> fields = Lists.newArrayList();

    public AttributeSection(ActivityFormDTO activity) {

        for (AttributeGroupDTO attributeGroup : activity.getAttributeGroups()) {

            AttributeField field;

            if (attributeGroup.isMultipleAllowed()) {

                AttributeCheckBoxGroup boxGroup = new AttributeCheckBoxGroup(attributeGroup);
                boxGroup.setStyleAttribute("marginBottom", "10px");
                boxGroup.setStyleAttribute("width", "100%"); // if the width is specified in px, IE6 flips out
                add(boxGroup);
                field = boxGroup;

            } else {
                AttributeCombo combo = new AttributeCombo(attributeGroup);
                add(combo);
                field = combo;
            }

            field.setReadOnly(attributeGroup.isWorkflow() && !activity.isDesignAllowed());

            fields.add(field);
        }
    }

    @Override
    public boolean validate() {
        boolean valid = true;
        for (AttributeField field : fields) {
            valid &= field.validate();
        }
        return valid;
    }

    @Override
    public void updateModel(SiteDTO site) {
        for (AttributeField field : fields) {
            field.updateModel(site);
        }
    }

    @Override
    public void updateForm(SiteDTO m, boolean isNew) {
        for (AttributeField field : fields) {
            field.updateForm(m, isNew);
        }
    }
}
