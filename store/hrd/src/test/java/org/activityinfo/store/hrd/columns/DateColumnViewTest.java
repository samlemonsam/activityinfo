package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.query.SortDir;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.store.testing.ColumnSetMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DateColumnViewTest {

    @Test
    public void sorting() {

        int dates[] = new int[] {
                DateEncoding.encodeLocalDate(new LocalDate(2017, 1, 3)),
                IntValueArray.MISSING,
                DateEncoding.encodeLocalDate(new LocalDate(2014, 3, 15)),
                DateEncoding.encodeLocalDate(new LocalDate(2014, 3, 1))
        };

        DateColumnView view = new DateColumnView(dates, new LocalDateRender());
        assertThat(view, ColumnSetMatchers.hasValues("2017-01-03", null, "2014-03-15", "2014-03-01"));

        int indexes[] = new int[] { 0, 1, 2, 3};
        view.order(indexes, SortDir.ASC, null);

        assertThat(view.getString(indexes[0]), nullValue());
        assertThat(view.getString(indexes[1]), equalTo("2014-03-01"));
        assertThat(view.getString(indexes[2]), equalTo("2014-03-15"));
        assertThat(view.getString(indexes[3]), equalTo("2017-01-03"));

    }


}