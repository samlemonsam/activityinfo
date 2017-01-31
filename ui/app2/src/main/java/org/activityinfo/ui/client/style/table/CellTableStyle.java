package org.activityinfo.ui.client.style.table;

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Strictness;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.user.cellview.client.CellTable;


@Source("CellTable.less")
@Strictness(ignoreMissingClasses = true)
public interface CellTableStyle extends Stylesheet, CellTable.Style {
}
