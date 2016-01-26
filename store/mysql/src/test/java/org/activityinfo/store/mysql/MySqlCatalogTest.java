package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.collections.CountryTable;
import org.activityinfo.store.mysql.collections.DatabaseTable;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityLoader;
import org.activityinfo.store.query.impl.Updater;
import org.hamcrest.CoreMatchers;
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
import static org.activityinfo.store.mysql.ColumnSetMatchers.hasAllNullValuesWithLengthOf;
import static org.activityinfo.store.mysql.ColumnSetMatchers.hasValues;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


public class MySqlCatalogTest extends AbstractMySqlTest {

    public static final int CATASTROPHE_NATURELLE_ID = 1;
 
    @BeforeClass
    public static void initDatabase() throws Throwable {
        resetDatabase();
    }
    
    @Test
    public void testCountry() {
        query(CountryTable.FORM_CLASS_ID, "label", "code");
        assertThat(column("label"), hasValues("Rdc"));
        assertThat(column("code"), hasValues("CD"));
    }

    @Test
    public void testDatabase() {
        query(DatabaseTable.FORM_CLASS_ID, "label", "description", "country", "country.label", "country.code");
        assertThat(column("label"), hasValues("PEAR", "PEAR Plus", "Alpha", "Public"));
        assertThat(column("description"), hasAllNullValuesWithLengthOf(4));
        assertThat(column("country"), hasValues("c0000000001", "c0000000001", "c0000000001", "c0000000001"));
        assertThat(column("country.label"), hasValues("Rdc", "Rdc", "Rdc", "Rdc"));
        assertThat(column("country.code"), hasValues("CD", "CD", "CD", "CD"));
    }

    @Test
    public void testLocation() {

        FormTreePrettyPrinter.print(queryFormTree(locationFormClass(1)));
        
        query(CuidAdapter.locationFormClass(1), "label", "axe", "territoire.province.name", "territoire.name",
                "province.name", "province._id", "visible");
        assertThat(column("label"), hasValues("Penekusu Kivu", "Ngshwe", "Boga", "Boga"));
        assertThat(column("axe"), hasValues(null, "Bunia-Wakombe", null, null));
        assertThat(column("territoire.name"), hasValues("Shabunda", null, "Irumu", "Bukavu"));
        assertThat(column("territoire.province.name"), hasValues("Sud Kivu", null, "Ituri", "Sud Kivu"));
        assertThat(column("province.name"), hasValues("Sud Kivu", "Kinshasa", "Ituri", "Sud Kivu"));
        assertThat(column("province._id"), hasValues("z0000000002", "z0000000001", "z0000000004", "z0000000002"));
        assertThat(column("visible"), hasValues(true, true, true, true));
    }

    @Ignore
    @Test
    public void testLocationPoints() {
        query(CuidAdapter.locationFormClass(1), "label", "point.latitude", "point.longitude");
    }
    
    @Test
    public void testAdmin() {

        query(CuidAdapter.adminLevelFormClass(2), "name", "province.name", "code", "boundary");
        assertThat(column("name"),          hasValues("Bukavu",   "Walungu",  "Shabunda", "Kalehe",   "Irumu"));
        assertThat(column("province.name"), hasValues("Sud Kivu", "Sud Kivu", "Sud Kivu", "Sud Kivu", "Ituri"));
        assertThat(column("code"), hasValues( "203", "201", "202", "203", "203"));
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
        FormTreeBuilder builder = new FormTreeBuilder(catalogProvider);
        FormTree formTree = builder.queryTree(classId);
        JsonObject formTreeObject = JsonFormTreeBuilder.toJson(formTree);
        formTree = JsonFormTreeBuilder.fromJson(formTreeObject);
        return formTree;
    }

    @Test
    public void testNoColumns() {
        QueryModel queryModel = new QueryModel(CuidAdapter.activityFormClass(1));
        columnSet = executor.build(queryModel);

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
        query(CuidAdapter.activityFormClass(1), "_id", "date1", "date2", "partner", 
                "partner.label", "location.label", "location.visible", "BENE", "cause", "project", "project.name");

        assertThat(column("_id"), hasValues(cuid(SITE_DOMAIN, 1), cuid(SITE_DOMAIN, 2), cuid(SITE_DOMAIN, 3)));
        assertThat(column("partner"), hasValues(partnerInstanceId(1), partnerInstanceId(1), partnerInstanceId(2)));
        assertThat(column("partner.label"), hasValues("NRC", "NRC", "Solidarites"));
        assertThat(column("location.label"), hasValues("Penekusu Kivu", "Ngshwe", "Boga"));
        assertThat(column("location.visible"), hasValues(true, true, true));
        assertThat(column("BENE"), hasValues(1500, 3600, 10000));
        assertThat(column("cause"), hasValues(null, "Deplacement", "Catastrophe Naturelle"));
        assertThat(column("project.name"), hasValues("USAID", "USAID", "RRMP"));
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
    public void testSingleSiteResource() throws IOException {
        ResourceId formClass = CuidAdapter.activityFormClass(1);
        ResourceUpdate update = new ResourceUpdate();
        update.setResourceId(cuid(SITE_DOMAIN, 1));
        update.set(field(formClass, PARTNER_FIELD), new ReferenceValue(CuidAdapter.partnerInstanceId(2)));
        update.set(indicatorField(1), new Quantity(900, "units"));
        update.set(attributeGroupField(1), new EnumValue(attributeId(CATASTROPHE_NATURELLE_ID)));

        Updater updater = new Updater(catalogProvider);
        updater.execute(update);

        query(CuidAdapter.activityFormClass(1), "_id", "partner", "BENE", "cause");

        assertThat(column("_id"), hasValues(cuid(SITE_DOMAIN, 1), cuid(SITE_DOMAIN, 2), cuid(SITE_DOMAIN, 3)));
        assertThat(column("partner"), hasValues(partnerInstanceId(2), partnerInstanceId(1), partnerInstanceId(2)));
        assertThat(column("BENE"), hasValues(900, 3600, 10000));
        assertThat(column("cause"), hasValues("Catastrophe Naturelle", "Deplacement", "Catastrophe Naturelle"));
    }
    
    @Test
    public void siteFormClassWithNullaryLocations() {

        FormClass formClass = catalogProvider.getCollection(activityFormClass(ADVOCACY)).get().getFormClass();
       
        // Make a list of field codes
        Set<String> codes = new HashSet<>();
        for (FormField formField : formClass.getFields()) {
            codes.add(formField.getCode());
        }
        assertThat(codes, not(hasItem("location")));
    }

    @Test
    public void testReportingPeriod() {

        FormTreeBuilder treeBuilder = new FormTreeBuilder(catalogProvider);
        FormTree formTree = treeBuilder.queryTree(CuidAdapter.reportingPeriodFormClass(3));

        FormTreePrettyPrinter.print(formTree);
        
        query(CuidAdapter.reportingPeriodFormClass(3), "rate", "date1", "date2", "site.partner", "site.partner.label",
                "site.location.label");
        
        assertThat(column("date1"), hasValues("2009-01-01", "2009-02-01", "2009-03-01"));
    }


    @Test
    public void testReportingPeriodWithDateFilter() {
        queryWhere(CuidAdapter.reportingPeriodFormClass(3), Arrays.asList(ColumnModel.ID_SYMBOL, "date1"), 
            "date1 > '2009-01-15'");
        assertThat(column(ColumnModel.ID_SYMBOL), hasValues("m0000000092", "m0000000093"));
        assertThat(column("date1"),               hasValues("2009-02-01", "2009-03-01"));
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
                CuidAdapter.indicatorField(1).asString(), "contact")    ;
        
    }


    @Test
    public void emptyIndicator() {
        query(CuidAdapter.activityFormClass(1), "_id", "q");
    }


    @Test
    public void ownerPermissions() {
        
        int ownerUserId = 1;
        CollectionPermissions permissions = 
                catalogProvider.getCollection(activityFormClass(1)).get().getPermissions(ownerUserId);

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.isEditAllowed(), equalTo(true));
        assertThat(permissions.getVisibilityFilter(), nullValue());
    }
    
    @Test
    public void noPermissions() {
        int userId = 21;

        CollectionPermissions permissions =
                catalogProvider.getCollection(activityFormClass(1)).get().getPermissions(userId);

        assertThat(permissions.isVisible(), equalTo(false));
        assertThat(permissions.isEditAllowed(), equalTo(false));
        assertThat(permissions.getVisibilityFilter(), nullValue());
        
    }

    @Test
    public void revokedPermissions() {
        int christianUserId = 5;

        CollectionPermissions permissions =
                catalogProvider.getCollection(activityFormClass(1)).get().getPermissions(christianUserId);

        assertThat(permissions.isVisible(), equalTo(false));
        assertThat(permissions.isEditAllowed(), equalTo(false));
        assertThat(permissions.getVisibilityFilter(), nullValue());
    }
    
    @Test
    public void editPartnerPermissions() {
        int userId = 4;

        CollectionPermissions permissions =
                catalogProvider.getCollection(activityFormClass(1)).get().getPermissions(userId);

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.isEditAllowed(), equalTo(true));
        assertThat(permissions.getVisibilityFilter(), CoreMatchers.equalTo("a00000000010000000007=p0000000002"));
        assertThat(permissions.getEditFilter(), CoreMatchers.equalTo("a00000000010000000007=p0000000002"));
    }


    @Test
    public void editAllPermissions() {
        int userId = 3;

        CollectionPermissions permissions =
                catalogProvider.getCollection(activityFormClass(1)).get().getPermissions(userId);

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.isEditAllowed(), equalTo(true));
        assertThat(permissions.getVisibilityFilter(), nullValue());
        assertThat(permissions.getEditFilter(),  nullValue());
    }
    
    @Test
    public void viewAllPermissions() {
        int userId = 2;
        CollectionPermissions permissions =
                catalogProvider.getCollection(activityFormClass(1)).get().getPermissions(userId);

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.isEditAllowed(), equalTo(true));
        assertThat(permissions.getVisibilityFilter(), nullValue());
    }
    
    @Test
    public void publicPermission() {
        ResourceId publicFormClassId = activityFormClass(41);
        CollectionPermissions permissions =
                catalogProvider.getCollection(publicFormClassId).get().getPermissions(999);
        
        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.getVisibilityFilter(), nullValue());
        assertThat(permissions.isEditAllowed(), equalTo(false));
    }
    
    @Test
    public void batchOpenCollections() {

        ResourceId activity1 = activityFormClass(1);
        ResourceId activity2 = activityFormClass(2);
        ResourceId provinceId = adminLevelFormClass(1);
        ResourceId monthlyId = reportingPeriodFormClass(4000);
        
        Map<ResourceId, FormClass> formClasses = catalogProvider.getFormClasses(
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

        Map<ResourceId, FormClass> formClasses = catalogProvider.getFormClasses(asList(
                adminLevelFormClass(1),
                adminLevelFormClass(2)));

        FormClass provinceClass = formClasses.get(adminLevelFormClass(1));
        FormClass territoryClass = formClasses.get(adminLevelFormClass(2));
        
        assertThat(provinceClass.getLabel(), equalTo("Province"));
        assertThat(territoryClass.getLabel(), equalTo("Territoire"));
    }
    
    
    @Test
    public void nonExistingSite() {

        Optional<ResourceCollection> collection = catalogProvider.lookupCollection(CuidAdapter.locationFormClass(9444441));
    
        assertFalse(collection.isPresent());
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

}
