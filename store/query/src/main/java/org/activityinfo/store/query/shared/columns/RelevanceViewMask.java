package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.SortModel;

/**
 * ColumnView that is conditioned on relevance expression
 */
public class RelevanceViewMask implements ColumnView {

    private ColumnView view;
    private ColumnView relevantView;

    public RelevanceViewMask(ColumnView view, ColumnView relevantView) {
        this.view = view;
        this.relevantView = relevantView;
        assert view.numRows() == relevantView.numRows();
    }

    @Override
    public ColumnType getType() {
        return view.getType();
    }

    @Override
    public int numRows() {
        return view.numRows();
    }

    @Override
    public Object get(int row) {
        return view.get(row);
    }

    @Override
    public double getDouble(int row) {
        if(isRelevant(row)) {
            return view.getDouble(row);
        } else {
            return Double.NaN;
        }
    }

    private boolean isRelevant(int row) {
        return relevantView.getBoolean(row) == TRUE;
    }

    @Override
    public String getString(int row) {
        if(isRelevant(row)) {
            return view.getString(row);
        } else {
            return null;
        }
    }

    @Override
    public int getBoolean(int row) {
        if(isRelevant(row)) {
            return view.getBoolean(row);
        } else {
            return NA;
        }
    }

    @Override
    public boolean isMissing(int row) {
        return !isRelevant(row) || view.isMissing(row);
    }

    @Override
    public ColumnView select(int[] rows) {
        return new RelevanceViewMask(view.select(rows), relevantView.select(rows));
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        return view.order(sortVector, direction, range);
    }
}