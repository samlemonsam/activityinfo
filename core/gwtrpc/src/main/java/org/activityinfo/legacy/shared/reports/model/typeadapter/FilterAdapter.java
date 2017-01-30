package org.activityinfo.legacy.shared.reports.model.typeadapter;

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

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.model.DateRange;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Bertram
 */
public class FilterAdapter extends XmlAdapter<FilterAdapter.FilterElement, Filter> {

    public static class Restriction {
        @XmlAttribute
        private String dimension;

        @XmlElement(name = "category")
        private List<String> categories = new ArrayList<String>(0);
    }

    public static class FilterElement {
        @XmlElement(name = "restriction")
        private List<Restriction> restrictions = new ArrayList<Restriction>(0);

        /**
         * Date range for end date. Note that the XML element is called "dateRange" and 
         * not "endDateRange" for backward compatibility.
         */
        @XmlElement(name = "dateRange")
        private DateRange endDateRange;
        
        @XmlElement
        private DateRange startDateRange;
    }

    @Override
    public Filter unmarshal(FilterElement element) throws Exception {
        Filter filter = new Filter();
        filter.setStartDateRange(element.startDateRange);
        filter.setEndDateRange(element.endDateRange);

        for (Restriction r : element.restrictions) {
            for (String s : r.categories) {
                filter.addRestriction(findDimType(r.dimension), Integer.parseInt(s));
            }
        }
        return filter;
    }

    private DimensionType findDimType(String name) {
        String nameLowered = name.toLowerCase();
        for (DimensionType type : DimensionType.values()) {
            if (type.toString().toLowerCase().equals(nameLowered)) {
                return type;
            }
        }
        throw new RuntimeException("No DimensionType could be find to match '" + name + "'");
    }

    @Override
    public FilterElement marshal(Filter filter) throws Exception {
        FilterElement element = new FilterElement();
        element.startDateRange = filter.getStartDateRange();
        element.endDateRange = filter.getEndDateRange();
        for (DimensionType t : filter.getRestrictedDimensions()) {
            Restriction r = new Restriction();
            r.dimension = t.toString().toLowerCase();
            for (Integer id : filter.getRestrictions(t)) {
                r.categories.add(id.toString());
            }
            element.restrictions.add(r);
        }
        return element;
    }
}
