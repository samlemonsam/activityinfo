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
package org.activityinfo.ui.client.page.entry.place;

import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.ui.client.page.PageStateParser;
import org.activityinfo.ui.client.page.entry.grouping.AdminGroupingModel;
import org.activityinfo.ui.client.page.entry.grouping.GroupingModel;

import java.util.Set;

/**
 * Serializes/deserializes the DataEntryPlace into a fragment that looks like:
 * <p/>
 * <pre>
 * all
 * all/page2
 * Activity+33-AdminLevel+14032+3242/page2
 * Activity+33-sort+Date2/page2
 * Activity+33-sortdesc+Date2/page2
 * </pre>
 */
public class DataEntryPlaceParser implements PageStateParser {

    public static String serialize(DataEntryPlace place) {
        StringBuilder fragment = new StringBuilder();
        appendGrouping(fragment, place.getGrouping());
        appendFilter(fragment, place.getFilter());

        return fragment.toString();
    }

    private static void appendGrouping(StringBuilder fragment, GroupingModel grouping) {
        if (grouping instanceof AdminGroupingModel) {
            if (fragment.length() > 0) {
                fragment.append("-");
            }
            fragment.append("groupByAdmin+").append(((AdminGroupingModel) grouping).getAdminLevelId());
        }
    }

    private static void appendFilter(StringBuilder fragment, Filter filter) {
        for (DimensionType dimType : filter.getRestrictedDimensions()) {
            if (fragment.length() > 0) {
                fragment.append("-");
            }
            fragment.append(dimType.name());
            Set<Integer> ids = filter.getRestrictions(dimType);
            for (Integer id : ids) {
                fragment.append("+").append(id);
            }
        }
    }

    @Override
    public DataEntryPlace parse(String token) {

        DataEntryPlace place = new DataEntryPlace();

        if (!token.isEmpty()) {

            String[] parts = token.split("/");

            new Filter();

            if (parts.length > 0) {
                String[] qualifiers = parts[0].split("\\-");
                for (String qualifier : qualifiers) {
                    String[] qualifierParts = qualifier.split("\\+");

                    if (qualifierParts[0].equals("groupByAdmin")) {
                        AdminGroupingModel grouping = new AdminGroupingModel(Integer.parseInt(qualifierParts[1]));
                        place.setGrouping(grouping);
                    } else {
                        updateFilter(place, qualifierParts);
                    }
                }
            }
        }
        return place;
    }

    private void updateFilter(DataEntryPlace place, String[] qualifierParts) {
        DimensionType type = DimensionType.valueOf(qualifierParts[0]);
        Set<Integer> ids = Sets.newHashSet();
        for (int i = 1; i < qualifierParts.length; ++i) {
            ids.add(Integer.parseInt(qualifierParts[i]));
        }
        place.getFilter().addRestriction(type, ids);
    }
}