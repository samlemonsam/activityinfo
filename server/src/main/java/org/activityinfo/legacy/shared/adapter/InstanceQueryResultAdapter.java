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
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.shared.Projection;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yuriyz on 4/10/14.
 */
public class InstanceQueryResultAdapter implements Function<List<Projection>, QueryResult<Projection>> {

    private final InstanceQuery query;

    public InstanceQueryResultAdapter(InstanceQuery query) {
        this.query = query;
    }

    @Nullable @Override
    public QueryResult<Projection> apply(List<Projection> list) {
        final int size = list.size();
        final int startIndex = query.getOffset() >= 0 && query.getOffset() < size ? query.getOffset() : 0;
        final int count = query.getMaxCount();
        final int endIndex = (startIndex + count) < size && (startIndex + count) >= 0 /* overflow check*/ ?
                (startIndex + count) : size;
        return new QueryResult<>(list.subList(startIndex, endIndex), size);
    }
}
