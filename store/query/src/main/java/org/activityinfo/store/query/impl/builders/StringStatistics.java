package org.activityinfo.store.query.impl.builders;

import com.google.common.collect.Sets;

import java.util.Set;

public class StringStatistics {

    public static final int UNIQUE_VALUE_LIMIT = 50;

    private int count;
    private int missingCount;

    private Set<Object> unique = Sets.newHashSet();

    /**
     * Stop tracking unique values after we hit 50 values
     */
    private boolean uniquish = true;

    public void update(Object value) {
        count++;
        if(value == null) {
            missingCount ++;
        } else {
            if(uniquish) {
                unique.add(value);
                if(unique.size() > UNIQUE_VALUE_LIMIT) {
                    uniquish = false;
                }
            }
        }
    }

    public boolean isEmpty() {
        return missingCount == count;
    }

    public boolean isConstant() {
        return unique.size() == 1 && missingCount == 0;
    }

}
