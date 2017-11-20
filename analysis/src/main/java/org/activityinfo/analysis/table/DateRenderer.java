package org.activityinfo.analysis.table;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Date;

public class DateRenderer implements ColumnRenderer<Date> {

    private final String id;
    private ColumnView view = null;

    public DateRenderer(String id) {
        this.id = id;
    }

    @Override
    public Date render(int rowIndex) {
        assert view != null : "updateColumnSet() has not been called";
        String text = view.getString(rowIndex);
        if(text == null) {
            return null;
        }
        return LocalDate.parse(text).atMidnightInMyTimezone();
    }

    @Override
    public void updateColumnSet(ColumnSet columnSet) {
        view = columnSet.getColumnView(id);
    }
}
