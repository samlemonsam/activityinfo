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
package org.activityinfo.store.mysql.metadata;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;
import java.util.Collection;

/**
 * Stores information on activities that are linked to this activity
 */
public class LinkedActivity implements Serializable {
    
    int activityId;

    /**
     * Map from destination indicators to their source indicators.
     */
    final Multimap<Integer, Integer> linkMap = HashMultimap.create();


    int reportingFrequency;


    public int getActivityId() {
        return activityId;
    }

    public int getReportingFrequency() {
        return reportingFrequency;
    }

    public ResourceId getLeafFormClassId() {
        switch (reportingFrequency) {
            case 0:
                return CuidAdapter.activityFormClass(activityId);
            case 1:
                return CuidAdapter.reportingPeriodFormClass(activityId);
        }
        throw new IllegalStateException("reportingFrequency: " + reportingFrequency);   
    }

    public Collection<Integer> getSourceIndicatorIdsFor(int destinationIndicatorId) {
        return linkMap.get(destinationIndicatorId);
    }

    public ResourceId getSiteFormClassId() {
        return CuidAdapter.activityFormClass(activityId);
    }
}
