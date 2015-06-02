package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Strings;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;

import java.util.Arrays;
import java.util.List;

/**
 * Unique tuple of values from the source collection used
 * to look up a referenced value.
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
