package org.activityinfo.legacy.shared.adapter;
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.promise.Promise;

import java.util.List;
import java.util.Set;

/**
 * @author yuriyz on 08/18/2015.
 */
public class ProjectionsByUniqueColumnFilter implements Function<List<Projection>, Promise<List<Projection>>> {

    private final FieldPath columnPath;

    public ProjectionsByUniqueColumnFilter(FieldPath columnPath) {
        this.columnPath = columnPath;
    }

    @Override
    public Promise<List<Projection>> apply(List<Projection> input) {
        List<Projection> filtered = Lists.newArrayList();
        Set<FieldValue> values = Sets.newHashSet();
        for (Projection projection : input) {
            FieldValue value = projection.getValue(columnPath);
            if (!values.contains(value)) {
                values.add(value);
                filtered.add(projection);
            }
        }
        return Promise.resolved(filtered);
    }
}
