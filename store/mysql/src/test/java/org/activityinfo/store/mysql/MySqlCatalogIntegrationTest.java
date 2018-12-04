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
package org.activityinfo.store.mysql;

import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.query.output.RowBasedJsonWriter;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.activityinfo.model.legacy.CuidAdapter.activityFormClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class MySqlCatalogIntegrationTest {

    private static DbUnit dbunit;
    private static ColumnSetBuilder columnSetBuilder;
    private static MySqlStorageProvider catalog;

    @BeforeClass
    public static void setup() throws Throwable {

        System.out.println("Running setup...");
        dbunit = new DbUnit();
        dbunit.openDatabase();
        dbunit.dropAllRows();
        dbunit.loadDatset(Resources.getResource(MySqlCatalogTest.class, "rdc.db.xml"));
        catalog = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
        columnSetBuilder = new ColumnSetBuilder(catalog, new NullFormSupervisor());
    }


    @Test
    public void testActivity() throws IOException {
        QueryModel model = new QueryModel(activityFormClass(33));
        model.selectField("date1");
        model.selectField("date2");
        model.selectExpr("Partner.name");

        FormTree formTree = new FormTreeBuilder(catalog).queryTree(activityFormClass(33));
        FormTreePrettyPrinter.print(formTree);

        FormClass formClass = catalog.getForm(activityFormClass(33)).get().getFormClass();
        for(FormField field : formClass.getFields()) {
            if(field.getType() instanceof QuantityType) {
                model.selectField(field.getId()).as("I" + CuidAdapter.getLegacyIdFromCuid(field.getId()));
            }
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        ColumnSet columnSet = columnSetBuilder.build(model);
        System.out.println("Query executed in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
        
        assertThat(columnSet.getNumRows(), equalTo(652));

        StringWriter stringWriter = new StringWriter();
        RowBasedJsonWriter writer = new RowBasedJsonWriter(stringWriter);
        writer.write(columnSet);
        
        System.out.println(stringWriter.toString());
    }

    @Test
    public void testSerializeActivity() throws SQLException, IOException {
        Activity activity = catalog.getActivityLoader().load(33);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(activity);
    }
}
