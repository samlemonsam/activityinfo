package org.activityinfo.core.shared.importing.source;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author yuriyz on 5/21/14.
 */
public class ColumnParser {

    private final PastedRow row;
    private List<SourceColumn> columns;

    public ColumnParser(PastedRow row) {
        this.row = row;
    }

    public PastedRow getRow() {
        return row;
    }

    public List<SourceColumn> parseColumns() {
        columns = Lists.newArrayList();
        for (int i = 0; i != row.getColumnCount(); ++i) {
            SourceColumn column = new SourceColumn();
            column.setIndex(i);
            column.setHeader(row.getColumnValue(i).trim());
            columns.add(column);
        }
        return columns;
    }
}
