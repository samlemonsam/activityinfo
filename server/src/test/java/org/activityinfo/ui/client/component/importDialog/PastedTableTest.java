package org.activityinfo.ui.client.component.importDialog;
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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.ui.client.component.importDialog.model.match.JvmConverterFactory;
import org.activityinfo.ui.client.component.importDialog.model.source.PastedTable;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

/**
 * @author yuriyz on 4/18/14.
 */
public class PastedTableTest {

    @Test
    public void parser() throws IOException {
        PastedTable pastedTable = new PastedTable(
                Resources.toString(getResource(getClass(), "qis.csv"), Charsets.UTF_8));
        pastedTable.parseAllRows();
        final List<SourceColumn> columns = pastedTable.getColumns();
        final List<? extends SourceRow> rows = pastedTable.getRows();

        Assert.assertEquals(columns.size(), 47);
        Assert.assertEquals(rows.size(), 63);
    }

    @Test
    public void columnTypeGuesser() throws IOException {
        PastedTable pastedTable = new PastedTable(
                Resources.toString(getResource(getClass(), "qis.csv"), Charsets.UTF_8));

        // guess column types
        pastedTable.parseAllRows();
        pastedTable.guessColumnsType(JvmConverterFactory.get());

        Assert.assertEquals(column(pastedTable, "Partner").getGuessedType(), FieldTypeClass.FREE_TEXT);
//        Assert.assertEquals(column(pastedTable, "_CREATION_DATE").getGuessedType(), FormFieldType.LOCAL_DATE);
        Assert.assertEquals(column(pastedTable, "_MODEL_VERSION").getGuessedType(), FieldTypeClass.QUANTITY);
    }

    protected SourceColumn column(PastedTable pastedTable, String header) {
        for (SourceColumn column : pastedTable.getColumns()) {
            if (column.getHeader().equals(header)) {
                return column;
            }
        }
        throw new RuntimeException("No column with header " + header);
    }

    @Test
    public void nfiParsingPerformance() throws IOException {
        long start = System.currentTimeMillis();
        PastedTable pastedTable = new PastedTable(
                Resources.toString(getResource(getClass(), "nfi-import-test.csv"), Charsets.UTF_8));
        pastedTable.parseAllRows();
        final List<SourceColumn> columns = pastedTable.getColumns();
        final List<? extends SourceRow> rows = pastedTable.getRows();
        long end = System.currentTimeMillis();

        System.out.println("Done in " + (end - start) + "ms , rows count=" + rows.size() + ", columns count=" + columns.size() );
    }

    @Test
    public void libreOfficeImport() throws IOException {
        PastedTable pastedTable = new PastedTable(
                Resources.toString(getResource(getClass(), "somali-camps.txt"), Charsets.UTF_8));
        pastedTable.parseAllRows();
        pastedTable.guessColumnsType(JvmConverterFactory.get());

        assertThat(pastedTable.getColumns(),
             contains(hasProperty("header", equalTo("Region")),
                     hasProperty("header", equalTo("District")),
                     hasProperty("header", equalTo("Village/camp")),
                     hasProperty("header", equalTo("Village Name")),
                     hasProperty("header", equalTo("Pcode")),
                     hasProperty("header", equalTo("Latitude")),
                     hasProperty("header", equalTo("Longitude"))));



    }
}
