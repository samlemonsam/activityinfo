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

import org.activityinfo.core.shared.Projection;

import java.util.List;

/**
 * @author yuriyz on 4/10/14.
 */
public class InstanceQueryResult {

    private final List<Projection> projections;
    private final int totalCount;

    public InstanceQueryResult(List<Projection> projections, int totalCount) {
        this.projections = projections;
        this.totalCount = totalCount;
    }

    public List<Projection> getProjections() {
        return projections;
    }

    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Full result where result contains all data (list.size == totalCount)
     *
     * @param projections projections
     * @return full result where result contains all data (list.size == totalCount)
     */
    public static InstanceQueryResult fullResult(List<Projection> projections) {
        return new InstanceQueryResult(projections, projections.size());
    }
}
