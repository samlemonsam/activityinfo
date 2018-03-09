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
package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.command.result.ActivityFormResults;

import java.util.Collection;
import java.util.Set;

/**
 * Fetches a list of forms based on the selected indicators
 * 
 */
public class GetActivityForms implements Command<ActivityFormResults> {
    
    private Filter filter;

    public GetActivityForms() {
    }

    public GetActivityForms(Filter filter) {
        this.filter = filter;
    }

    public GetActivityForms(Set<Integer> indicatorIds) {
        filter = new Filter();
        filter.addRestriction(DimensionType.Indicator, indicatorIds);
    }

    public Filter getFilter() {
        return filter;
    }

    public GetActivityForms setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }
    
    public Collection<Integer> getIndicators() {
        return filter.getRestrictions(DimensionType.Indicator);
    }
    
    public Collection<Integer> getActivities() {
        return filter.getRestrictions(DimensionType.Activity);
    }
}
