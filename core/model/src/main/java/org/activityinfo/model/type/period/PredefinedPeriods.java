package org.activityinfo.model.type.period;
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

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;

/**
 * @author yuriyz on 01/27/2015.
 */
public enum PredefinedPeriods {

    YEARLY(new PeriodValue().setYear(1), "Yearly"),
    MONTHLY(new PeriodValue().setMonth(1), "Monthly"),
    BI_WEEKLY(new PeriodValue().setWeek(2), "Each two weeks"),
    WEEKLY(new PeriodValue().setWeek(1), "Weekly"),
    DAILY(new PeriodValue().setDay(1), "Daily"),
    HOURLY(new PeriodValue().setHour(1), "Hourly");

    private final PeriodValue period;
    private final String label;

    PredefinedPeriods(PeriodValue period, String label) {
        this.period = period;
        this.label = label;
    }

    public PeriodValue getPeriod() {
        return period;
    }

    public String getLabel() {
        return label;
    }

    public ResourceId getResourceId() {
        return ResourceIdPrefixType.SUBFORM.id("_period_" + this.name());
    }

    public static PredefinedPeriods fromPeriod(PeriodValue value) {
        for (PredefinedPeriods period : values()) {
            if (period.getPeriod().equals(value)) {
                return period;
            }
        }
        return null;
    }

    public static boolean isPeriodId(ResourceId resourceId) {
        return resourceId != null && resourceId.asString().startsWith(ResourceIdPrefixType.SUBFORM.id("_period_").asString());
    }
}
