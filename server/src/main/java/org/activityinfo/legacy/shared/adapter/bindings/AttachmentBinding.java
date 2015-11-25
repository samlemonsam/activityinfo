package org.activityinfo.legacy.shared.adapter.bindings;
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

import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.attachment.AttachmentValue;

import java.util.Map;

/**
 * @author yuriyz on 11/18/2015.
 */
public class AttachmentBinding implements FieldBinding<SiteDTO> {

    private final ResourceId fieldId;
    private final String propertyName;

    public AttachmentBinding(ResourceId fieldId, String propertyName) {
        this.fieldId = fieldId;
        this.propertyName = propertyName;
    }

    @Override
    public void updateInstanceFromModel(FormInstance instance, SiteDTO model) {
        Object value = model.get(propertyName);
        if (value instanceof String) {
            try {
                instance.set(fieldId, AttachmentValue.fromRecord(Resources.recordFromJson((String) value)));
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void populateChangeMap(FormInstance instance, Map<String, Object> changeMap) {
        Object value = instance.get(fieldId);
        if (value != null) {
            if (value instanceof AttachmentValue) {
                value = Resources.toJsonObject(((AttachmentValue) value).asRecord()).toString();
            } else {
                throw new UnsupportedOperationException("Unknown value type of attachments: " +
                        fieldId + " = " + value.getClass().getSimpleName());
            }
        }
        changeMap.put(propertyName, value);
    }
}
