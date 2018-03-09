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
package org.activityinfo.io.xls;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

public class XlsColumn {

    private String heading;
    private XlsColumnRenderer renderer;
    private CellStyle style;

    public XlsColumn(String heading, CellStyle style, XlsColumnRenderer renderer) {
        this.heading = heading;
        this.style = style;
        this.renderer = renderer;
    }


    public String getHeading() {
        return heading;
    }

    public boolean isMissing(int row) {
        return renderer.isMissing(row);
    }

    public CellStyle getStyle() {
        return style;
    }

    public void setValue(Cell cell, int row) {
        renderer.setValue(cell, row);
    }
}
