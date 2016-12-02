package org.activityinfo.core.client;
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

import java.util.List;

/**
 * @author yuriyz on 4/10/14.
 */
public class QueryResult<T> {

    private final List<T> items;
    private final int totalCount;

    public QueryResult(List<T> projections, int totalCount) {
        this.items = projections;
        this.totalCount = totalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Full result where result contains all data (list.size == totalCount)
     *
     * @param projections items
     * @return full result where result contains all data (list.size == totalCount)
     */
    public static <T> QueryResult<T> fullResult(List<T> projections) {
        return new QueryResult<>(projections, projections.size());
    }

    public boolean hasNext(int offset) {
        return (offset + getItems().size()) < getTotalCount();
    }
}
