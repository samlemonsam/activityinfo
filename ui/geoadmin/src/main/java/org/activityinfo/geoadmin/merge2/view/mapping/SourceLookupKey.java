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
package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Strings;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;

import java.util.Arrays;
import java.util.List;

/**
 * Unique tuple of values from the source collection used to look up a referenced value.
 * 
 * 
 */
public class SourceLookupKey {
    private final String[] values;
    private final int hashCode;

    public SourceLookupKey(String[] values) {
        this.values = values;
        this.hashCode = Arrays.hashCode(values);
    }
    public static SourceLookupKey build(List<FieldProfile> sourceKeyFields, int rowIndex) {
        String[] values = new String[sourceKeyFields.size()];
        for(int fieldIndex=0;fieldIndex<sourceKeyFields.size();++fieldIndex) {
            values[fieldIndex] = Strings.nullToEmpty(sourceKeyFields.get(fieldIndex).getView().getString(rowIndex));
        }
        return new SourceLookupKey(values);
    }
    
    public String get(int fieldIndex) {
        return values[fieldIndex];
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SourceLookupKey)) {
            return false;
        }
        SourceLookupKey that = (SourceLookupKey) obj;
        return Arrays.equals(this.values, that.values);
    }


}
