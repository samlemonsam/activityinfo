package org.activityinfo.model.form;
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

import java.util.Comparator;

/**
 * @author yuriyz on 01/22/2016.
 */
public class FormInstanceComparators {

    private FormInstanceComparators() {
    }

    public static Comparator<FormInstance> doubleComparator(final ResourceId sortField) {
        return new Comparator<FormInstance>() {
            @Override
            public int compare(FormInstance o1, FormInstance o2) {
                Double d1 = o1.getDouble(sortField);
                Double d2 = o2.getDouble(sortField);
                if (d1 != null && d2 != null) {
                    return d1.compareTo(d2);
                }
                return 0;
            }
        };
    }

    public static Comparator<FormInstance> stringComparator(final ResourceId sortField) {
        return new Comparator<FormInstance>() {
            @Override
            public int compare(FormInstance o1, FormInstance o2) {
                String s1 = o1.getString(sortField);
                String s2 = o2.getString(sortField);
                if (s1 != null && s2 != null) {
                    return s1.compareTo(s2);
                }
                return 0;
            }
        };
    }
}
