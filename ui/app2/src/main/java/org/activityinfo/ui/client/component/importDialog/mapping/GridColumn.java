/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.component.importDialog.mapping;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;

class GridColumn extends Column<SourceRow, String> {
    private SourceColumn column;

    public GridColumn(SourceColumn column) {
        super(new TextCell() {
            @Override
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.append(SafeHtmlUtils.fromTrustedString("<div title='" + value.asString() + "'>"));
                    sb.append(value);
                    sb.appendHtmlConstant("</div>");
                }
            }
        });
        this.column = column;
    }

    @Override
    public String getValue(SourceRow row) {
        return row.getColumnValue(column.getIndex());
    }
}
