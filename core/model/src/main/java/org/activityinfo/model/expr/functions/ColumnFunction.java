package org.activityinfo.model.expr.functions;

import org.activityinfo.model.query.ColumnView;

import java.util.List;

/**
 * Function which can be applied to an entire column of values at once
 */
public interface ColumnFunction {

  /**
   * Apply this function to all rows in the provided arguments, 
   */
  ColumnView columnApply(List<ColumnView> arguments);
}
