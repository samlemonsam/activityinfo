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

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.io.Resources;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.metadata.CountryStructure;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.FormSupervisorAdapter;
import org.activityinfo.store.query.shared.FormScanCache;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.spi.UserDatabaseProvider;
import org.activityinfo.store.testing.MockUserDatabaseProvider;
import org.junit.After;
import org.junit.Before;

import java.util.List;

public abstract class AbstractMySqlTest {

    public static final int ADVOCACY = 4000;

    public final UserDatabaseProvider userDatabaseProvider = new MockUserDatabaseProvider();

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalMemcacheServiceTestConfig(),
                    new LocalDatastoreServiceTestConfig()
                        .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));


    public static DbUnit dbunit;
    public ColumnSet columnSet;
    public static MySqlStorageProvider catalog;
    
    private Closeable objectify;

    private int userId = 1;

    protected FormScanCache cache = new NullFormScanCache();


    @Before
    public void setUp() {
        helper.setUp();
        objectify = ObjectifyService.begin();
    }

    @After
    public void tearDown() {
        objectify.close();
        helper.tearDown();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public static void resetDatabase(String resourceName) throws Throwable {

        System.out.println("Running setup...");
        dbunit = new DbUnit();
        dbunit.openDatabase();
        dbunit.dropAllRows();
        dbunit.loadDatset(Resources.getResource(MySqlCatalogTest.class, resourceName));
        catalog = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
        CountryStructure.clearCache();
    }

    protected final void newRequest() {
        catalog = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
    }


    protected final void query(ResourceId formClassId, String... fields) {
        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectRecordId().as("_id");
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        query(queryModel);
    }

    protected final void queryWhere(ResourceId formClassId, List<String> fields, String filter) {
        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectRecordId().as("_id");
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        queryModel.setFilter(filter);

        query(queryModel);
    }


    protected final void query(QueryModel queryModel) {

        ColumnSetBuilder builder = new ColumnSetBuilder(
                catalog,
                cache,
                new FormSupervisorAdapter(catalog, userDatabaseProvider, userId));

        columnSet = builder.build(queryModel);

        for(String field : columnSet.getColumns().keySet()) {
            System.out.println(field + ": " + column(field));
        }
    }


    protected final ColumnView column(String column) {
        return columnSet.getColumnView(column);
    }
    
}
