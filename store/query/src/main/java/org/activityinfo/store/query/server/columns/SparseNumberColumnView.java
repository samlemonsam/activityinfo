package org.activityinfo.store.query.server.columns;


import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.FilteredColumnView;

class SparseNumberColumnView extends AbstractNumberColumn {

    private final int numRows;
    private final Int2DoubleOpenHashMap map;

    SparseNumberColumnView(double[] elements, int numRows, int numMissing) {

        this.numRows = numRows;
        this.map = new Int2DoubleOpenHashMap(numRows - numMissing);
        this.map.defaultReturnValue(Double.NaN);

        for (int i = 0; i < numRows; i++) {
            double value = elements[i];
            if(!Double.isNaN(value)) {
                map.put(i, value);
            }
        }
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public double getDouble(int row) {
        return map.get(row);
    }

    @Override
    public boolean isMissing(int row) {
        return !map.containsKey(row);
    }

    @Override
    public ColumnView select(int[] rows) {
        return new FilteredColumnView(this, rows);
    }
}
