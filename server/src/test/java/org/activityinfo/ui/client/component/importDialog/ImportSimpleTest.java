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

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.MapExistingAction;
import org.activityinfo.ui.client.component.importDialog.model.match.ColumnMappingGuesser;
import org.activityinfo.ui.client.component.importDialog.model.match.JvmConverterFactory;
import org.activityinfo.ui.client.component.importDialog.model.source.PastedTable;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImportStrategies;
import org.activityinfo.ui.client.component.importDialog.model.strategy.ImportTarget;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static org.activityinfo.promise.PromiseMatchers.assertResolves;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

//@SuppressWarnings("GwtClientClassFromNonInheritedModule")
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/brac-import.db.xml")
public class ImportSimpleTest extends AbstractImporterTest {

    private static final ResourceId HOUSEHOLD_SURVEY_FORM_CLASS = CuidAdapter.activityFormClass(1);
    public static final ResourceId VILLAGE_FORM_ID = CuidAdapter.locationFormClass(2);
    public static final ResourceId PROVINCE_FORM_ID = CuidAdapter.adminLevelFormClass(3);

    private static final ResourceId TRAINING_PROGRAM_CLASS = CuidAdapter.activityFormClass(2);


    private static final ResourceId BRAC_PARTNER_RESOURCE_ID = CuidAdapter.partnerRecordId(1);

    public static final int MODHUPUR = 24;

    @Test
    public void test() throws IOException {

        FormTree formTree = assertResolves(locator.getFormTree(HOUSEHOLD_SURVEY_FORM_CLASS));
        FormTreePrettyPrinter.print(formTree);


        importModel = new ImportModel(formTree);


        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource(getClass(), "qis.csv"), Charsets.UTF_8));
        source.parseAllRows();

        assertThat(source.getRows().size(), equalTo(63));

        importModel.setSource(source);
        importer = new Importer(locator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));

        dumpList("COLUMNS", source.getColumns());

        // Step 2: User maps imported columns to FormFields
        dumpList("FIELDS", importer.getImportTargets());
        importModel.setColumnAction(columnIndex("MEMBER_NO_ADULT_FEMALE"), target("NumAdultMale"));
        importModel.setColumnAction(columnIndex("MEMBER_NO_ADULT_FEMALE"), target("NumAdultFemale"));
        importModel.setColumnAction(columnIndex("_CREATION_DATE"), target("Start Date"));
        importModel.setColumnAction(columnIndex("_SUBMISSION_DATE"), target("End Date"));
        importModel.setColumnAction(columnIndex("district name"), target("District Name"));
        importModel.setColumnAction(columnIndex("upazila"), target("Upzilla Name"));
        importModel.setColumnAction(columnIndex("Partner"), target("Partner Name"));


        // Step 3: Validate for user
        ValidatedRowTable validatedResult = assertResolves(importer.validateRows(importModel));
        showValidationGrid(validatedResult);

        assertResolves(importer.persist(importModel));

        // VERIFY total count
        SiteResult allResults = execute(new GetSites(Filter.filter().onActivity(1)));
        assertThat(allResults.getData().size(), equalTo(63));

        // AND... verify
        Filter filter = new Filter();
        filter.addRestriction(DimensionType.AdminLevel, MODHUPUR);

        SiteResult sites = execute(new GetSites(filter));
        assertThat(sites.getTotalLength(), equalTo(1));

        SiteDTO site = sites.getData().get(0);
        assertThat(site.getDate1(), equalTo(new LocalDate(2012,12,19)));
        assertThat(site.getDate2(), equalTo(new LocalDate(2012,12,19)));
        assertThat(site.getAdminEntity(3), not(nullValue()));
    }

    @Test
    public void locationWithMissingAdminLevel() throws IOException {

        FormTree formTree = assertResolves(locator.getFormTree(VILLAGE_FORM_ID));
        FormTreePrettyPrinter.print(formTree);


        importModel = new ImportModel(formTree);


        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource(getClass(), "qis-villages.csv"), Charsets.UTF_8));
        source.parseAllRows();

        assertThat(source.getRows().size(), equalTo(1));

        importModel.setSource(source);
        importer = new Importer(locator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));

        dumpList("COLUMNS", source.getColumns());

        // Step 2: User maps imported columns to FormFields
        List<ImportTarget> targets = importer.getImportTargets();
        dumpList("FIELDS", targets);
        importModel.setColumnAction(columnIndex("Name"), target("Name"));
        importModel.setColumnAction(columnIndex("District"), target("District Name"));


        // Step 3: Validate for user
        ValidatedRowTable validatedResult = assertResolves(importer.validateRows(importModel));
        showValidationGrid(validatedResult);

        assertResolves(importer.persist(importModel));

        // AND... verify
        QueryModel queryModel = new QueryModel(VILLAGE_FORM_ID);
        queryModel.selectExpr("Name").as("name");
        queryModel.selectField(CuidAdapter.field(VILLAGE_FORM_ID, CuidAdapter.ADMIN_FIELD)).as("admin");

        ColumnSet columnSet = assertResolves(locator.queryTable(queryModel));
        assertThat(columnSet.getNumRows(), equalTo(1));
        assertThat(columnSet.getColumnView("name").getString(0), equalTo("Village 1"));
        assertThat(columnSet.getColumnView("admin").getString(0),
                equalTo(CuidAdapter.cuid(CuidAdapter.ADMIN_ENTITY_DOMAIN, 2).asString()));


    }

    @Test
    public void testExceptionHandling() throws IOException {


        FormTree formTree = assertResolves(locator.getFormTree(HOUSEHOLD_SURVEY_FORM_CLASS));
        importModel = new ImportModel(formTree);

        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource(getClass(), "qis.csv"), Charsets.UTF_8));

        importModel.setSource(source);
        importer = new Importer(locator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));
        importModel.setColumnAction(columnIndex("MEMBER_NO_ADULT_FEMALE"), target("NumAdultMale"));
        importModel.setColumnAction(columnIndex("MEMBER_NO_ADULT_FEMALE"), target("NumAdultFemale"));
        importModel.setColumnAction(columnIndex("_CREATION_DATE"), target("Start Date"));
        importModel.setColumnAction(columnIndex("_SUBMISSION_DATE"), target("End Date"));
        importModel.setColumnAction(columnIndex("district name"), target("District Name"));
        importModel.setColumnAction(columnIndex("upazila"), target("Upzilla Name"));
       // importModel.setColumnAction(columnIndex("Partner"), target("Partner Name"));

        Promise<Void> result = importer.persist(importModel);
        assertThat(result.getState(), equalTo(Promise.State.REJECTED));
    }

    @Test
    public void columnMappingGuesser() throws IOException {
        FormTree formTree = assertResolves(locator.getFormTree(HOUSEHOLD_SURVEY_FORM_CLASS));
        FormTreePrettyPrinter.print(formTree);

        importModel = new ImportModel(formTree);

        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource(getClass(), "qis.csv"), Charsets.UTF_8));

        importModel.setSource(source);
        importer = new Importer(locator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));

        dumpList("COLUMNS", source.getColumns());
        dumpList("FIELDS", importer.getImportTargets());

        // Step 2: Guesser guess mapping
        final ColumnMappingGuesser guesser = new ColumnMappingGuesser(importModel, importer.getImportTargets());
        guesser.guess();

        assertMapping("Partner", "Partner Name");
        assertMapping("district name", "District Name");
    }

    private void assertMapping(String sourceColumnLabel, String targetColumnLabel) {
        final SourceColumn sourceColumn = importModel.getSourceColumn(columnIndex(sourceColumnLabel));
        assertNotNull(sourceColumn);

        final MapExistingAction columnAction = (MapExistingAction) importModel.getColumnAction(sourceColumn);
        assertTrue(columnAction.getTarget().getLabel().equals(targetColumnLabel));
    }

}
