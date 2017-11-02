package org.activityinfo.server.command.handler.binding.dim;

import org.activityinfo.legacy.shared.reports.content.*;
import org.activityinfo.legacy.shared.reports.model.DateDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.metadata.Activity;
import org.joda.time.LocalDate;

import java.util.Collections;
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
        ResourceId classId = formTree.getRootFormId();

        return Collections.singletonList(new ColumnModel()
                .setExpression(CuidAdapter.field(classId, CuidAdapter.END_DATE_FIELD))
                .as(DATE_COLUMN_ID));
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return Collections.singletonList(new ColumnModel()
                .setExpression(new SymbolExpr("toDate"))
                .as(DATE_COLUMN_ID));
    }

    @Override
    public DimensionCategory[] extractCategories(Activity activity, ColumnSet columnSet) {

        ColumnView column = columnSet.getColumnView(DATE_COLUMN_ID);

        DimensionCategory[] c = new DimensionCategory[column.numRows()];
        for (int i = 0; i < column.numRows(); i++) {
            c[i] = categoryOf(column.getString(i));
        }
        return c;
    }

    @Override
    public DimensionCategory extractTargetCategory(Activity activity, ColumnSet columnSet, int rowIndex) {
        return categoryOf(columnSet.getColumnView(DATE_COLUMN_ID).getString(rowIndex));
    }

    private DimensionCategory categoryOf(String dateString) {
        if(dateString == null) {
            return null;
        }
        
        LocalDate localDate = new LocalDate(dateString);

        switch (model.getUnit()) {
            case YEAR:
                return new YearCategory(localDate.getYear());
            case QUARTER:
                return new QuarterCategory(localDate.getYear(), quarterOf(localDate.getMonthOfYear()));
            case MONTH:
                return new MonthCategory(localDate.getYear(), localDate.getMonthOfYear());
            case WEEK_MON:
                return new WeekCategory(localDate.getWeekyear(), localDate.getWeekOfWeekyear());
            case DAY:
                return new DayCategory(localDate.toDate());
        }
        throw new UnsupportedOperationException();
    }


    private int quarterOf(int monthOfYear) {
        int quarter0 = (monthOfYear - 1) / 3;
        return quarter0 + 1;
    }
}
