package org.activityinfo.ui.client.style.table;

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Strictness;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.user.cellview.client.DataGrid;

/**
 * Application style sheet for the Data Grid
 */
@Source("datagrid.less")
@Strictness(ignoreMissingClasses = true)
public interface DataGridStylesheet extends Stylesheet, DataGrid.Style {
}
