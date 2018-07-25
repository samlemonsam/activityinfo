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
package org.activityinfo.store.hrd;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.TypedRecordUpdate;
import org.activityinfo.store.testing.IntakeForm;
import org.activityinfo.store.testing.RecordGenerator;
import org.activityinfo.store.testing.Survey;
import org.junit.*;

import static org.activityinfo.store.testing.ColumnSetMatchers.hasValues;
import static org.hamcrest.MatcherAssert.assertThat;

public class HrdFormStorageTest {

    private static final int USER_ID = 1;
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));
    
    private int userId = 1;
    private Closeable objectifyCloseable;

    @Before
    public void setUp() {
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();
    }
    
    @BeforeClass
    public static void setUpLocale() {
        LocaleProxy.initialize();
    }

    @After
    public void tearDown() {
        helper.tearDown();
        objectifyCloseable.close();
    }

    @Test
    @Ignore("Requires column block queries to be enabled")
    public void surveyTest() {

        Survey surveyForm = new Survey();

        HrdStorageProvider storageProvider = new HrdStorageProvider();
        HrdFormStorage storage = storageProvider.create(surveyForm.getFormClass());

        RecordGenerator generator = surveyForm.getGenerator();

        int numRecords = 20;

        for (int i = 0; i < numRecords; i++) {
            FormInstance newRecord = generator.get();
            System.out.println(newRecord);
            storage.add(new TypedRecordUpdate(USER_ID, newRecord));
        }

        QueryModel queryModel = new QueryModel(surveyForm.getFormId());
        queryModel.selectRecordId().as("id");
        queryModel.selectField("gender").as("gender");

        ColumnSetBuilder builder = new ColumnSetBuilder(storageProvider, new NullFormSupervisor());
        ColumnSet columnSet = builder.build(queryModel);

        assertThat(columnSet.getColumnView("gender"), hasValues(
                "Female", null, "Male", "Female",
                "Female", "Male", "Female", "Male",
                "Female", "Male", "Female", "Female",
                "Female", null, null, "Male",
                "Female", "Male", null, "Female"));

    }


    @Test
    public void intakeForm() {

        IntakeForm intakeForm = new IntakeForm();

        HrdStorageProvider storageProvider = new HrdStorageProvider();
        HrdFormStorage storage = storageProvider.create(intakeForm.getFormClass());

        RecordGenerator generator = intakeForm.getGenerator();

        int numRecords = 5;

        for (int i = 0; i < numRecords; i++) {
            FormInstance newRecord = generator.get();
            System.out.println(newRecord);
            storage.add(new TypedRecordUpdate(USER_ID, newRecord));
        }

        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
        queryModel.selectRecordId().as("id");
        queryModel.selectField("OPENED").as("OPENED");
        queryModel.selectField("DOB").as("DOB");
        queryModel.selectExpr("nationality.Palestinian").as("PAL");
        queryModel.selectExpr("nationality.syrian").as("SYR");
        queryModel.selectExpr("address").as("ADR");

        ColumnSetBuilder builder = new ColumnSetBuilder(storageProvider, new NullFormSupervisor());
        ColumnSet columnSet = builder.build(queryModel);

        assertThat(columnSet.getColumnView("OPENED"), hasValues("2016-11-07", "2016-11-23", "2017-10-27", "2017-07-06", "2017-10-12"));
        assertThat(columnSet.getColumnView("DOB"), hasValues("1971-11-07", null, null, "1987-12-18", "1978-01-23"));
        assertThat(columnSet.getColumnView("PAL"), hasValues(false, true, true, true, true));

        assertThat(columnSet.getColumnView("ADR"), hasValues(
                "1534 Tualco Ave\nPenny, EG 69609",
                "1154 Northeast Keswick Boulevard\nNorth Dogwood, CH 81482",
                "1150 West Entwistle St.\nCedar Valley, BT 70636",
                "1258 Scenic St.\nWestridge, OW 62470",
                "1090 Lawton Ave\nEast Galer, FH  1834"));

    }
}
