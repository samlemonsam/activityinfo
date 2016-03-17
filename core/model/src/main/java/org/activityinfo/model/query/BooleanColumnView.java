package org.activityinfo.model.query;

import org.activityinfo.model.type.geo.Extents;

/**
 * Boolean column view backed by a int[] array
 */
public class BooleanColumnView implements ColumnView {
  
  private final int values[];

  public BooleanColumnView(int[] values) {
    this.values = values;
  }

  @Override
  public ColumnType getType() {
    return ColumnType.BOOLEAN;
  }

  @Override
  public int numRows() {
    return values.length;
  }

  @Override
  public Object get(int row) {
    int v = values[row];
    if(v == ColumnView.NA) {
      return null;
    } else {
      return v != 0;
    }
  }

  @Override
  public double getDouble(int row) {
    int v = values[row];
    if(v == ColumnView.NA) {
      return Double.NaN;
    } else {
      return v;
    }
  }

  @Override
  public String getString(int row) {
    return null;
  }

  @Override
  public Extents getExtents(int row) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getBoolean(int row) {
    return values[row];
  }
}
