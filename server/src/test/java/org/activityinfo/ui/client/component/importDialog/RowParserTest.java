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
package org.activityinfo.ui.client.component.importDialog;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import org.activityinfo.ui.client.component.importDialog.model.source.PastedRow;
import org.activityinfo.ui.client.component.importDialog.model.source.PastedTable;
import org.activityinfo.ui.client.component.importDialog.model.source.RowParser;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class RowParserTest {

    @Test
    public void simpleCsv() {

        List<PastedRow> rows = new RowParser("hello,brave,new,world\n1,2,3,4\n5,6,7,8", ',').parseAllRows();

        assertThat(rows.size(), equalTo(3));
        assertThat(rows.get(0).getColumnValue(0), equalTo("hello"));
        assertThat(rows.get(0).getColumnValue(1), equalTo("brave"));
        assertThat(rows.get(0).getColumnValue(3), equalTo("world"));
        assertThat(rows.get(1).getColumnValue(0), equalTo("1"));
        assertThat(rows.get(1).getColumnValue(1), equalTo("2"));
        assertThat(rows.get(1).getColumnValue(3), equalTo("4"));
        assertThat(rows.get(2).getColumnValue(3), equalTo("8"));
    }

    @Test
    public void quotedFields() {
        List<PastedRow> rows = new RowParser("\"hello, fred\",bob,\"hello, there\"\na,b,c", ',').parseAllRows();

        assertThat(rows.size(), equalTo(2));
        assertThat(rows.get(0).getColumnValue(0), equalTo("hello, fred"));
        assertThat(rows.get(0).getColumnValue(1), equalTo("bob"));
        assertThat(rows.get(0).getColumnValue(2), equalTo("hello, there"));
        assertThat(rows.get(1).getColumnValue(0), equalTo("a"));
    }

    @Test
    public void quotedFieldsWithNewlines() {
        List<PastedRow> rows = new RowParser(
                "1,Jane Goodall,\"304 E42nd street\nNew York, NY\nUSA\"\n" +
                "2,Richard Feynman,\"401 1st Street\nCaltech\nUSA\"", ',').parseAllRows();

        System.out.println(Joiner.on('\n').join(rows));

        assertThat(rows.size(), equalTo(2));
        assertThat(rows.get(0).getColumnValue(2), equalTo("304 E42nd street\n" +
                "New York, NY\n" +
                "USA"));

        assertThat(rows.get(1).getColumnValue(0), equalTo("2"));

    }

    @Test
    public void qisCsvColumns() throws IOException {
        PastedTable source = new PastedTable(
                Resources.toString(getResource(getClass(), "qis.csv"), Charsets.UTF_8));

        final List<SourceColumn> columns = source.getColumns();
        System.out.println(Joiner.on('\n').join(columns));
        assertThat(columns.size(), equalTo(47));
    }
}
