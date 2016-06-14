package org.activityinfo.legacy.shared.command;

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

import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.command.PivotSites.PivotResult;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.reports.model.Dimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PivotSites implements Command<PivotResult> {
    private static final long serialVersionUID = 563469767187419658L;

    public enum ValueType {
        INDICATOR,
        TOTAL_SITES
    }

    private Set<Dimension> dimensions;
    private Filter filter;
    private ValueType valueType = ValueType.INDICATOR;

    public PivotSites() {
    }

    public PivotSites(Set<Dimension> dimensions, Filter filter) {
        super();
        this.dimensions = dimensions;
        this.filter = filter;
    }

    public Set<Dimension> getDimensions() {
        return dimensions;
    }

    public Set<DimensionType> getDimensionTypes() {
        Set<DimensionType> set = Sets.newHashSet();
        for (Dimension dim : getDimensions()) {
            set.add(dim.getType());
        }
        return set;
    }

    public void setDimensions(Set<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public void setDimensions(Dimension... dimensions) {
        this.dimensions = Sets.newHashSet(dimensions);
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public boolean isTooBroad() {
        return !getFilter().isRestricted(DimensionType.Database) &&
                !getFilter().isRestricted(DimensionType.Activity) &&
                !getFilter().isRestricted(DimensionType.Indicator);
    }

    @Override
    public String toString() {
        return "PivotSites [dimensions=" + dimensions + ", filter=" + filter + ", valueType=" + valueType + "]";
    }

    public boolean isPivotedBy(DimensionType dimType) {
        for (Dimension dim : dimensions) {
            if (dim.getType() == dimType) {
                return true;
            }
        }
        return false;
    }

    public static class PivotResult implements CommandResult {
        private static final long serialVersionUID = -7767105395264612561L;

        private List<Bucket> buckets;

        public PivotResult() {
            this(new ArrayList<Bucket>());
        }

        public PivotResult(List<Bucket> buckets) {
            this.buckets = buckets;
        }

        public List<Bucket> getBuckets() {
            return buckets;
        }

        public void setBuckets(List<Bucket> buckets) {
            this.buckets = buckets;
        }
    }

}
