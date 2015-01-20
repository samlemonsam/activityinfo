package org.activityinfo.store.mysql;

import org.activityinfo.model.query.ColumnView;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public class ColumnSetMatchers {

    public static TypeSafeMatcher<ColumnView> hasValues(final String... values) {
        return new TypeSafeMatcher<ColumnView>() {
            @Override
            protected boolean matchesSafely(ColumnView item) {
                if(item.numRows() != values.length) {
                    return false;
                }
                for(int i=0;i!=values.length;++i) {
                    if(!Objects.equals(item.getString(i), values[i])) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("column with values").appendValueList("[", ", ", "]", values);
            }
        };
    }

}
