package org.activityinfo.ui.client.page.entry.form.field;

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

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.activityinfo.legacy.shared.model.AttributeDTO;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;

public class AttributeCombo extends ComboBox<AttributeDTO> implements AttributeField {

    AttributeDTO defaultValue = null;

    public AttributeCombo(AttributeGroupDTO attributeGroup) {
        super();
        String name = attributeGroup.getName();
        if (attributeGroup.isMandatory()) {
            name += "*";
            this.setAllowBlank(false);
        }
        this.setFieldLabel(Format.htmlEncode(name));
        this.setDisplayField("name");
        this.setTriggerAction(TriggerAction.ALL);
        this.setEditable(false);

        ListStore<AttributeDTO> store = new ListStore<AttributeDTO>();
        store.add(attributeGroup.getAttributes());

        setStore(store);

        if (attributeGroup.getDefaultValue() != null) {
            defaultValue = attributeGroup.getAttributeById(attributeGroup.getDefaultValue());
        }
    }

    @Override
    public void updateForm(SiteDTO site, boolean isNew) {
        for (AttributeDTO attribute : getStore().getModels()) {
            if (site.getAttributeValue(attribute.getId())) {
                setValue(attribute);
                return;
            }
        }
        if (isNew) {
            setValue(defaultValue);
        } else {
            setValue(null);
        }
    }

    @Override
    public void updateModel(SiteDTO site) {
        AttributeDTO selected = getValue();

        for (AttributeDTO attribute : getStore().getModels()) {
            site.setAttributeValue(attribute.getId(), selected != null && selected.getId() == attribute.getId());
        }
    }
}
