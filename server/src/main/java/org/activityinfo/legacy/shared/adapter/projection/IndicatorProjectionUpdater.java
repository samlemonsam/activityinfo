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
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;

/**
 * @author yuriyz on 6/3/14.
 */
public class IndicatorProjectionUpdater implements ProjectionUpdater<Object> {

    private FieldPath path;
    private int indicatorId;

    public IndicatorProjectionUpdater(FieldPath path, int indicatorId) {
        this.path = path;
        this.indicatorId = indicatorId;
    }

    public int getIndicatorId() {
        return indicatorId;
    }

    @Override
    public void update(Projection projection, Object value) {
        if(value instanceof Number) {
            projection.setValue(path, new Quantity(((Number) value).doubleValue()));
        } else if(value instanceof String) {
            projection.setValue(path, TextValue.valueOf(((String) value)));
        } else if(value != null) {
            throw new IllegalArgumentException("type: " + value.getClass().getName());
        }
    }
}
