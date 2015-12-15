package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.reports.content.*;
import org.activityinfo.legacy.shared.reports.model.DateDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Date;
import java.util.List;


public class DateDimBinding extends DimBinding {

    private static final String DATE_COLUMN_ID = "Date";

    private final DateDimension model;

    public DateDimBinding(DateDimension model) {
        this.model = model;
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        ResourceId classId = formTree.getRootFormClass().getId();

        return Collections.singletonList(new ColumnModel()
                .setExpression(CuidAdapter.field(classId, CuidAdapter.END_DATE_FIELD))
                .as(DATE_COLUMN_ID));
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, FormTree formTree, ColumnSet columnSet) {

        ColumnView column = columnSet.getColumnView(DATE_COLUMN_ID);

        DimensionCategory[] c = new DimensionCategory[column.numRows()];
        for (int i = 0; i < column.numRows(); i++) {
            Date date = column.getDate(i);
            LocalDate localDate = new LocalDate(date);
            
            switch (model.getUnit()) {
                case YEAR:
                    c[i] = new YearCategory(localDate.getYear());
                    break;
                case QUARTER:
                    c[i] = new QuarterCategory(localDate.getYear(), quarterOf(localDate.getMonthOfYear()));
                    break;
                case MONTH:
                    c[i] = new MonthCategory(localDate.getYear(), localDate.getMonthOfYear());
                    break;
                case WEEK_MON:
                    c[i] = new WeekCategory(localDate.getYear(), localDate.getWeekOfWeekyear());
                    break;
                case DAY:
                    c[i] = new DayCategory(date);
                    break;
            }
        }
        return c;
    }

    private int quarterOf(int monthOfYear) {
        int quarter0 = (monthOfYear - 1) / 3;
        return quarter0 + 1;
    }
}
