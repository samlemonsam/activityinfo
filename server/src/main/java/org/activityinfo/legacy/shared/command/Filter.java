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
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.legacy.shared.reports.model.typeadapter.FilterAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * Defines a filter of activity data as a date range and a set of restrictions
 * on <code>Dimensions</code>.
 */
@XmlJavaTypeAdapter(FilterAdapter.class)
public class Filter implements Serializable {
    private static final long serialVersionUID = -9117480720562024905L;

    // TODO: should be restrictions on DIMENSIONS and not DimensionTypes!!
    private Map<DimensionType, Set<Integer>> restrictions = new HashMap<DimensionType, Set<Integer>>();

    private DateRange endDateRange = new DateRange();
    
    private DateRange startDateRange = new DateRange();

    /**
     * Constructs a <code>Filter</code> with no restrictions. All data visible
     * to the user will be included.
     */
    public Filter() {
    }

    /**
     * Constructs a copy of the given <code>filter</code>
     *
     * @param filter The filter which to copy.
     */
    public Filter(Filter filter) {

        for (Map.Entry<DimensionType, Set<Integer>> entry : filter.restrictions.entrySet()) {
            this.restrictions.put(entry.getKey(), new HashSet<Integer>(entry.getValue()));
        }
        this.startDateRange = filter.startDateRange;
        this.endDateRange = filter.endDateRange;
    }

    /**
     * Constructs a <code>Filter</code> as the intersection between two
     * <code>Filter</code>s.
     *
     * @param a The first filter
     * @param b The second filter
     */
    public Filter(Filter a, Filter b) {

        Set<DimensionType> types = new HashSet<DimensionType>();
        types.addAll(a.restrictions.keySet());
        types.addAll(b.restrictions.keySet());

        for (DimensionType type : types) {
            this.restrictions.put(type, intersect(a.getRestrictionSet(type, false), b.getRestrictionSet(type, false)));

        }
        this.endDateRange = DateRange.intersection(a.getEndDateRange(), b.getEndDateRange());
        this.startDateRange = DateRange.intersection(a.getStartDateRange(), b.getStartDateRange());

    }

    private Set<Integer> intersect(Set<Integer> a, Set<Integer> b) {
        if (a.size() == 0) {
            return new HashSet<Integer>(b);
        }
        if (b.size() == 0) {
            return new HashSet<Integer>(a);
        }

        Set<Integer> intersection = new HashSet<Integer>(a);
        intersection.retainAll(b);

        return intersection;
    }

    public Set<Integer> getRestrictions(DimensionType type) {
        return getRestrictionSet(type, false);
    }

    private Set<Integer> getRestrictionSet(DimensionType type, boolean create) {
        Set<Integer> set = restrictions.get(type);

        if (set == null) {
            if (!create) {
                return Collections.emptySet();
            }
            set = new HashSet<Integer>();
            restrictions.put(type, set);
        }

        return set;
    }

    public void addRestriction(DimensionType type, int categoryId) {
        Set<Integer> set = getRestrictionSet(type, true);
        set.add(categoryId);
    }

    public void addRestriction(DimensionType type, Collection<Integer> categoryIds) {
        if (!categoryIds.isEmpty()) {
            Set<Integer> set = getRestrictionSet(type, true);
            set.addAll(categoryIds);
        }
    }

    public void clearRestrictions(DimensionType type) {
        restrictions.remove(type);
    }

    public boolean isRestricted(DimensionType type) {
        if (type == DimensionType.Date) {
            return isEndDateRestricted();
        } else {
            Set<Integer> set = restrictions.get(type);
            return set != null && !set.isEmpty();
        }
    }

    public boolean isNull() {
        return restrictions.isEmpty() && !isEndDateRestricted();
    }

    public boolean hasRestrictions() {
        return !restrictions.isEmpty();
    }

    public boolean isEndDateRestricted() {
        return endDateRange != null && endDateRange.isRestricted();
    }
    
    public boolean isStartDateRestricted() {
        return startDateRange != null && startDateRange.isRestricted();
    }

    public Set<DimensionType> getRestrictedDimensions() {
        Set<DimensionType> dims = Sets.newHashSet();
        for (Entry<DimensionType, Set<Integer>> entries : restrictions.entrySet()) {
            if (!entries.getValue().isEmpty()) {
                dims.add(entries.getKey());
            }
        }
        return dims;
    }

    public Map<DimensionType, Set<Integer>> getRestrictions() {
        return restrictions;
    }

    public void setEndDateRange(DateRange range) {
        this.endDateRange = range;
    }

    public boolean isDimensionRestrictedToSingleCategory(DimensionType type) {
        return getRestrictions(type).size() == 1;
    }

    /**
     * @throws UnsupportedOperationException if the dimension is not restricted to exactly one category
     */
    public int getRestrictedCategory(DimensionType type) {
        Set<Integer> ids = getRestrictions(type);
        if (ids.size() != 1) {
            throw new UnsupportedOperationException(
                    "Cannot return a unique category, the dimension " + type + " is restricted to " +
                    ids.size() + " categories");
        }
        return ids.iterator().next();
    }

    public DateRange getEndDateRange() {
        if (endDateRange == null) {
            endDateRange = new DateRange();
        }
        return endDateRange;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DimensionType type : getRestrictedDimensions()) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(type.toString()).append("={");
            for (Integer id : getRestrictions(type)) {
                sb.append(' ').append(id);
            }
            sb.append(" }");
        }
        toString("startDate", startDateRange, sb);
        toString("endDate", endDateRange, sb);
        if (sb.length() != 0) {
            sb.append(", ");
        }
        sb.insert(0, "[");
        sb.append("]");
        return sb.toString();
    }

    private void toString(final String fieldName, DateRange dateRange, StringBuilder sb) {
        if (dateRange != null && (dateRange.getMinDate() != null || dateRange.getMaxDate() != null)) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(fieldName + "=[");
            if (dateRange.getMinDate() != null) {
                sb.append(dateRange.getMinDate());
            }
            sb.append(",");
            if (dateRange.getMaxDate() != null) {
                sb.append(dateRange.getMaxDate()).append("]");
            }
        }
    }

    public Filter onActivity(int activityId) {
        addRestriction(DimensionType.Activity, activityId);
        return this;
    }

    public Filter onSite(int siteId) {
        addRestriction(DimensionType.Site, siteId);
        return this;
    }

    public Filter onDatabase(int databaseId) {
        addRestriction(DimensionType.Database, databaseId);
        return this;
    }

    public DateRange getStartDateRange() {
        if(startDateRange == null) {
            startDateRange = new DateRange();
        }
        return startDateRange;
    }

    public void setStartDateRange(DateRange startDateRange) {
        this.startDateRange = startDateRange;
    }

    public static Filter filter() {
        return new Filter();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endDateRange == null) ? 0 : endDateRange.hashCode());
        result = prime * result + ((startDateRange == null) ? 0 : startDateRange.hashCode());
        result = prime * result + ((restrictions == null) ? 0 : restrictions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Filter other = (Filter) obj;
        return getEndDateRange().equals(other.getEndDateRange()) && 
                getStartDateRange().equals(other.getStartDateRange()) &&
                getRestrictions().equals(other.getRestrictions());
    }

}
