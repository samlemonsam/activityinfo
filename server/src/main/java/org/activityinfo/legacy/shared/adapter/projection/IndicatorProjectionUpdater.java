package org.activityinfo.legacy.shared.adapter.projection;
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

import org.activityinfo.core.shared.Projection;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;

/**
 * @author yuriyz on 6/3/14.
 */
public class IndicatorProjectionUpdater implements ProjectionUpdater<Object> {

    private FieldPath path;
    private IndicatorDTO indicator;

    public IndicatorProjectionUpdater(FieldPath path, IndicatorDTO indicator) {
        this.path = path;
        this.indicator = indicator;
    }

    public int getIndicatorId() {
        return indicator.getId();
    }

    @Override
    public void update(Projection projection, Object value) {
        if(value instanceof Number) {
            projection.setValue(path, new Quantity(((Number) value).doubleValue()));
        } else if(value instanceof String) {
            if (indicator.getType() == AttachmentType.TYPE_CLASS) {
                projection.setValue(path, AttachmentValue.fromJson((String) value));
            } else if (indicator.getType() == ReferenceType.TYPE_CLASS) {
                projection.setValue(path, ReferenceValue.fromJson((String) value));
            } else {
                projection.setValue(path, TextValue.valueOf(((String) value)));
            }
        } else if(value != null) {
            throw new IllegalArgumentException("type: " + value.getClass().getName());
        }
    }
}
