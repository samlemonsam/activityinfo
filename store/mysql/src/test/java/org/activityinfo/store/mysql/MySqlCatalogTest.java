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

import com.google.common.base.Optional;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.HrdStorageProvider;
import org.activityinfo.store.mysql.collections.CountryTable;
import org.activityinfo.store.mysql.mapping.TableMigrator;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityLoader;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.VersionedFormStorage;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.activityinfo.store.testing.ColumnSetMatchers.hasValues;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class MySqlCatalogTest extends AbstractMySqlTest {


    protected int userId = 1;

    @BeforeClass
    public static void initDatabase() throws Throwable {
        resetDatabase("catalog-test.db.xml");
    }

    @Test
    public void testCountry() {
        query(CountryTable.FORM_CLASS_ID, "label", "code");
        assertThat(column("label"), hasValues("Rdc"));
        assertThat(column("code"), hasValues("CD"));
    }

    @Test
    public void testLocation() {

        cache = new TestingFormScanCache();

        System.out.println("***** testLocation() starting ******* ");

        FormTreePrettyPrinter.print(queryFormTree(locationFormClass(1)));

        query(CuidAdapter.locationFormClass(1), "label", "axe", "territoire.province.name", "territoire.name",
                "province.name", "province._id", "visible");

        System.out.println("***** testLocation() done ******* ");


        assertThat(column("label"), hasValues("Penekusu Kivu", "Ngshwe", "Boga", "Boga"));
        assertThat(column("axe"), hasValues(null, "Bunia-Wakombe", null, null));
        assertThat(column("territoire.name"), hasValues("Shabunda", null, "Irumu", "Bukavu"));
        assertThat(column("territoire.province.name"), hasValues("Sud Kivu", null, "Ituri", "Sud Kivu"));
        assertThat(column("province.name"), hasValues("Sud Kivu", "Kinshasa", "Ituri", "Sud Kivu"));
        assertThat(column("province._id"), hasValues("z0000000002", "z0000000001", "z0000000004", "z0000000002"));


        // Verify that all is loaded from cache.
        query(CuidAdapter.locationFormClass(1), "label", "axe", "territoire.province.name", "territoire.name",
                "province.name", "province._id", "visible");
    }

    @Test
    public void testBoundLocation() {
        ResourceId formClassId = CuidAdapter.activityFormClass(41);
        FormTree tree = this.queryFormTree(formClassId);
        FormTree.Node locationNode = tree.getRootField(CuidAdapter.locationField(41));

        FormTreePrettyPrinter.print(tree);

        assertTrue(locationNode.getRange().contains(CuidAdapter.adminLevelFormClass(2)));

        query(formClassId, "territoire.name", "territoire.province.name");

        assertThat(column("_id"), hasValues("s0000009001", "s0000009002", "s0000009003"));
        assertThat(column("territoire.name"), hasValues("Bukavu", "Walungu", "Shabunda"));
        assertThat(column("territoire.province.name"), hasValues("Sud Kivu", "Sud Kivu", "Sud Kivu"));
    }

    @Test
    public void testLocationPoints() {
        query(CuidAdapter.locationFormClass(1), "label", "point.latitude", "point.longitude");
    }

    @Test
    public void testAdmin() {

        query(CuidAdapter.adminLevelFormClass(2), "name", "province.name", "code",
                "ST_XMIN(boundary)",
                "ST_YMIN(boundary)",
                "ST_XMAX(boundary)",
                "ST_YMAX(boundary)");
        assertThat(column("name"),          hasValues("Bukavu",   "Walungu",  "Shabunda", "Kalehe",   "Irumu"));
        assertThat(column("province.name"), hasValues("Sud Kivu", "Sud Kivu", "Sud Kivu", "Sud Kivu", "Ituri"));
        assertThat(column("code"), hasValues("203", "201", "202", "203", "203"));
        assertThat(column("ST_XMIN(boundary)"), hasValues(0, 0, 0, 33.5, 0));
        assertThat(column("ST_XMAX(boundary)"), hasValues(0, 0, 0, -44.0, 0));
        assertThat(column("ST_YMIN(boundary)"), hasValues(0, 0, 0, -22.0, 0));
        assertThat(column("ST_YMAX(boundary)"), hasValues(0, 0, 0, 40, 0));
    }

    @Test
    public void testAdminTree() {
        FormTree formTree = queryFormTree(activityFormClass(1));

        FormTreePrettyPrinter.print(formTree);
    }

    @Test
    public void project() {
        query(CuidAdapter.projectFormClass(1), "name", "description");

        assertThat(column("name"), hasValues("RRMP", "USAID", "Kivu water"));
    }


    private FormTree queryFormTree(ResourceId classId) {
        FormTreeBuilder builder = new FormTreeBuilder(catalog);
        FormTree formTree = builder.queryTree(classId);
        JsonValue formTreeObject = JsonFormTreeBuilder.toJson(formTree);
        formTree = JsonFormTreeBuilder.fromJson(formTreeObject);
        return formTree;
    }

    @Test
    public void testNoColumns() {
        QueryModel queryModel = new QueryModel(CuidAdapter.activityFormClass(1));

        query(queryModel);

        assertThat(columnSet.getNumRows(), equalTo(3));
    }

    @Test
    public void testActivitySerialization() throws SQLException, IOException {
        ActivityLoader loader = new ActivityLoader(dbunit.getExecutor());
        Map<Integer, Activity> map = loader.load(Collections.singleton(1));
        Activity activity = map.get(1);

        ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        oos.writeObject(activity);
    }


    @Test
    public void testSiteSimple() {
        query(CuidAdapter.activityFormClass(1), "_id", "_class", "date1", "date2", "partner",
                "partner.label", "location.label", "location.visible", "BENE", "cause", "project", "project.name");

        assertThat(column("_id"), hasValues(cuid(SITE_DOMAIN, 1), cuid(SITE_DOMAIN, 2), cuid(SITE_DOMAIN, 3)));
        assertThat(column("_class"), hasValues("a0000000001", "a0000000001", "a0000000001"));
        assertThat(column("partner"), hasValues(partnerRecordId(1), partnerRecordId(1), partnerRecordId(2)));
        assertThat(column("partner.label"), hasValues("NRC", "NRC", "Solidarites"));
        assertThat(column("location.label"), hasValues("Penekusu Kivu", "Ngshwe", "Boga"));
        assertThat(column("BENE"), hasValues(1500, 3600, 10000));
        assertThat(column("cause"), hasValues(null, "Deplacement", "Catastrophe Naturelle"));
        assertThat(column("project.name"), hasValues("USAID", "USAID", "RRMP"));
    }

    @Test
    public void testSiteLocation() {
        query(CuidAdapter.activityFormClass(1), "_id", "location.latitude", "location.longitude");

        assertThat(column("_id"), hasValues(cuid(SITE_DOMAIN, 1), cuid(SITE_DOMAIN, 2), cuid(SITE_DOMAIN, 3)));
        assertThat(column("location.latitude"), hasValues(27.323, 28.232, null));
        assertThat(column("location.longitude"), hasValues(1.532,  1.323, null));
    }

    @Test
    public void testAttributeBoolean() {
        query(CuidAdapter.activityFormClass(1), "_id", "[Contenu du Kit].Casserole", "[Contenu du Kit].Soap");

        assertThat(column("[Contenu du Kit].Casserole"), hasValues(true, false, false));
        assertThat(column("[Contenu du Kit].Soap"), hasValues(true, true, false));

    }


    @Test
    public void testAttributeBooleanWithIf() {
        query(CuidAdapter.activityFormClass(1), "_id", "IF([Contenu du Kit].Casserole, 10, 0)");

        assertThat(column("IF([Contenu du Kit].Casserole, 10, 0)"), hasValues(10, 0, 0));

    }

    @Ignore
    @Test
    public void testSiteAggregated() {
        query(CuidAdapter.activityFormClass(1), "project.name", "sum(BENE)", "project.name");

    }

    @Test
    public void testSiteFilteredOnPartner() {
        queryWhere(CuidAdapter.activityFormClass(1),
                asList("_id", "partner.name", "project.name"), "partner.name == 'NRC'");

        assertThat(column("_id"), hasValues(cuid(SITE_DOMAIN, 1), cuid(SITE_DOMAIN, 2)));

    }

    @Test
    public void singleSite() {
        FormStorage siteStorage = catalog.getForm(CuidAdapter.activityFormClass(1)).get();
        FormRecord siteRecord = siteStorage.get(CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, 1)).get();
        TypedFormRecord site = TypedFormRecord.toTypedFormRecord(siteStorage.getFormClass(), siteRecord);

        EnumValue cause = (EnumValue) site.get(CuidAdapter.attributeGroupField(1));
        EnumValue kitContents = (EnumValue) site.get(CuidAdapter.attributeGroupField(2));

        assertThat(cause, nullValue());
        assertThat(kitContents.getResourceIds(), contains(
            CuidAdapter.attributeId(3),
            CuidAdapter.attributeField(4)));
    }

    @Test
    public void singleSiteWithBoundLocation() {
        FormStorage siteStorage = catalog.getForm(CuidAdapter.activityFormClass(4)).get();
        FormRecord siteRecord = siteStorage.get(CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, 6)).get();
        TypedFormRecord site = TypedFormRecord.toTypedFormRecord(siteStorage.getFormClass(), siteRecord);

        FieldValue location = site.get(CuidAdapter.locationField(4));
    }


    @Test
    public void siteFormClassWithNullaryLocations() {

        FormClass formClass = catalog.getForm(activityFormClass(ADVOCACY)).get().getFormClass();

        // Make a list of field codes
        Set<String> codes = new HashSet<>();
        for (FormField formField : formClass.getFields()) {
            codes.add(formField.getCode());
        }
        assertThat(codes, not(hasItem("location")));
    }

    @Test
    public void testReportingPeriod() {

        FormTreeBuilder treeBuilder = new FormTreeBuilder(catalog);
        FormTree formTree = treeBuilder.queryTree(CuidAdapter.reportingPeriodFormClass(3));

        FormTreePrettyPrinter.print(formTree);

        query(CuidAdapter.reportingPeriodFormClass(3), "rate", "date1", "date2", "site.partner", "site.partner.label",
                "site.location.label");

        assertThat(column("date1"), hasValues("2009-01-01", "2009-02-01", "2009-03-01"));
    }


    @Test
    public void testReportingPeriodWithDateFilter() {
        queryWhere(CuidAdapter.reportingPeriodFormClass(3), Arrays.asList(ColumnModel.RECORD_ID_SYMBOL, "date1"),
                "date1 > '2009-01-15'");
        assertThat(column(ColumnModel.RECORD_ID_SYMBOL), hasValues("m0000000092", "m0000000093"));
        assertThat(column("date1"), hasValues("2009-02-01", "2009-03-01"));
    }

    @Test
    public void testReportingPeriodCached() {

        String[] columns = {
                "rate", "date1", "date2", "site.partner",
                "site.partner.label",
                "site.location.label"};

        // First time cache will be cold
        query(CuidAdapter.reportingPeriodFormClass(3), columns);

        // Query again to verify that memcache works....
        query(CuidAdapter.reportingPeriodFormClass(3), columns);
    }

    @Test
    public void testSiteIndicator() {
        query(CuidAdapter.activityFormClass(1), "_id",
                CuidAdapter.indicatorField(1).asString(), "contact");

    }

    @Test
    public void emptyIndicator() {
        query(CuidAdapter.activityFormClass(1), "_id", "q");
    }

    @Test
    public void malformedFormId() {

        Optional<FormStorage> form = catalog.getForm(ResourceId.valueOf("a1"));
        assertTrue("form should not exist", !form.isPresent());
    }

    @Test
    public void batchOpenCollections() {

        ResourceId activity1 = activityFormClass(1);
        ResourceId activity2 = activityFormClass(2);
        ResourceId provinceId = adminLevelFormClass(1);
        ResourceId monthlyId = reportingPeriodFormClass(4000);

        Map<ResourceId, FormClass> formClasses = catalog.getFormClasses(
                asList(activity1, activity2, provinceId, monthlyId));

        FormClass activityFormClass1 = formClasses.get(activity1);
        assertThat(activityFormClass1, notNullValue());
        assertThat(activityFormClass1.getLabel(), equalTo("NFI"));

        FormClass activityFormClass2 = formClasses.get(activity2);
        assertThat(activityFormClass2, notNullValue());
        assertThat(activityFormClass2.getLabel(), equalTo("Distribution de Kits Scolaire"));

        FormClass provinceFormClass = formClasses.get(provinceId);
        assertThat(provinceFormClass.getLabel(), equalTo("Province"));

        FormClass monthlyForm = formClasses.get(monthlyId);
        assertThat(monthlyForm, Matchers.notNullValue());
    }

    @Test
    public void batchOpenAdminLevels() {

        Map<ResourceId, FormClass> formClasses = catalog.getFormClasses(asList(
                adminLevelFormClass(1),
                adminLevelFormClass(2)));

        FormClass provinceClass = formClasses.get(adminLevelFormClass(1));
        FormClass territoryClass = formClasses.get(adminLevelFormClass(2));

        assertThat(provinceClass.getLabel(), equalTo("Province"));
        assertThat(territoryClass.getLabel(), equalTo("Territoire"));
    }


    @Test
    public void nonExistingSite() {

        Optional<FormStorage> storage = catalog.getForm(CuidAdapter.locationFormClass(1));
        Optional<FormRecord> record = storage.get().get(CuidAdapter.locationFormClass(9444441));

        assertFalse(record.isPresent());
    }

    @Test
    public void singlePartner() {

        FormStorage form = catalog.getForm(CuidAdapter.partnerFormId(1)).get();
        Optional<FormRecord> partnerRecord = form.get(CuidAdapter.partnerRecordId(1));

    }

    @Test
    public void partnerVersionRange() {
        ResourceId partnerFormId = CuidAdapter.partnerFormId(1);
        VersionedFormStorage form = (VersionedFormStorage) catalog.getForm(partnerFormId).get();

        System.out.println("version = " + form.cacheVersion());

        assertThat(form.cacheVersion(), greaterThan(0L));

        FormSyncSet versionRange = form.getVersionRange(0, form.cacheVersion(), resourceId -> true,
                java.util.Optional.empty());

        assertThat(versionRange.getUpdatedRecordCount(), equalTo(3));

        System.out.println(versionRange);

    }

    @Test
    public void targets() {
        String targetValue12451 = CuidAdapter.targetIndicatorField(12451).asString();
        
        query(CuidAdapter.cuid(CuidAdapter.TARGET_FORM_CLASS_DOMAIN, 1), "_id", "name", "fromDate", "toDate",
                targetValue12451, "partner.name");

        assertThat(column("_id"), hasValues(cuid(TARGET_INSTANCE_DOMAIN, 6001)));
        assertThat(column("name"), hasValues("Long term goals"));
        assertThat(column("fromDate"), hasValues("2009-01-01"));
        assertThat(column("toDate"), hasValues("2012-01-01"));
        assertThat(column(targetValue12451), hasValues(9999));
        assertThat(column("partner.name"), hasValues("NRC"));
    }

    @Test
    public void migrationTest() throws SQLException {
        ResourceId formId = adminLevelFormClass(1);

        TableMigrator.migrate(formId, catalog.getExecutor());

        //  <adminEntity adminLevelId="1" adminEntityId="4" name="Ituri"
        //               code="40" x1="0" y1="0" x2="0" y2="0"/>

        HrdStorageProvider hrdStorageProvider = new HrdStorageProvider();
        FormStorage formStorage = hrdStorageProvider.getForm(formId).get();

        FormRecord ituri = formStorage.get(entity(4)).get();
        TypedFormRecord typedIturi = TypedFormRecord.toTypedFormRecord(formStorage.getFormClass(), ituri);

        assertThat(typedIturi.get(CuidAdapter.field(formId, NAME_FIELD)), equalTo(TextValue.valueOf("Ituri")));
        assertThat(typedIturi.get(CuidAdapter.field(formId, CODE_FIELD)), equalTo(TextValue.valueOf("40")));
        assertThat(typedIturi.get(CuidAdapter.field(formId, GEOMETRY_FIELD)), equalTo(
                new GeoArea(new Extents(0, 0, 0, 0))));


        System.out.println(ituri);
    }
}
