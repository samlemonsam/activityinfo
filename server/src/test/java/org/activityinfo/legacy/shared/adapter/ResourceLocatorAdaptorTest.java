package org.activityinfo.legacy.shared.adapter;


import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.GetLocations;
import org.activityinfo.legacy.shared.command.result.LocationResult;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class ResourceLocatorAdaptorTest extends CommandTestCase2 {

    private static final int CAUSE_ATTRIBUTE_GROUP_ID = 1;

    private static final int PROVINCE_ADMIN_LEVEL_ID = 1;

    private static final ResourceId PROVINCE_CLASS = CuidAdapter.adminLevelFormClass(PROVINCE_ADMIN_LEVEL_ID);

    private static final int PEAR_DATABASE_ID = 1;

    private static final int HEALTH_CENTER_LOCATION_TYPE = 1;

    private static final ResourceId HEALTH_CENTER_CLASS = CuidAdapter.locationFormClass(HEALTH_CENTER_LOCATION_TYPE);

    private static final int NFI_DIST_ID = 1;

    private static final ResourceId NFI_DIST_FORM_CLASS = CuidAdapter.activityFormClass(NFI_DIST_ID);

    public static final int VILLAGE_TYPE_ID = 1;

    public static final ResourceId VILLAGE_CLASS = CuidAdapter.locationFormClass(VILLAGE_TYPE_ID);

    public static final int IRUMU = 21;

    
    @Test
    @OnDataSet("/dbunit/jordan-locations.db.xml")
    public void getLocation() {
        ResourceId classId = locationFormClass(50512);
        FormInstance instance = assertResolves(locator.getFormInstance(classId, locationInstanceId(1590565828)));
        Set<ResourceId> adminUnits = instance.getReferences(field(classId, ADMIN_FIELD));
        System.out.println(adminUnits);
    }


    @Test
    @OnDataSet("/dbunit/sites-calculated-indicators.db.xml")
    public void persistSiteWithCalculatedIndicators() {
        int siteId = new KeyGenerator().generateInt();
        FormInstance instance = new FormInstance(CuidAdapter.cuid(SITE_DOMAIN, siteId), NFI_DIST_FORM_CLASS);

        instance.set(indicatorField(1), 1);
        instance.set(indicatorField(2), 2);
        instance.set(locationField(NFI_DIST_ID), locationInstanceId(1));
        instance.set(partnerField(NFI_DIST_ID), partnerInstanceId(1));
        instance.set(projectField(NFI_DIST_ID), projectInstanceId(1));
        instance.set(field(NFI_DIST_FORM_CLASS, START_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(field(NFI_DIST_FORM_CLASS, END_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(field(NFI_DIST_FORM_CLASS, COMMENT_FIELD), NarrativeValue.valueOf("My comment"));

        assertResolves(locator.persist(instance));

        FormInstance firstRead = assertResolves(locator.getFormInstance(NFI_DIST_FORM_CLASS, instance.getId()));

        assertThat(firstRead.get(indicatorField(1)), equalTo((FieldValue)new Quantity(1, "menages")));
        assertThat(firstRead.get(indicatorField(2)), equalTo((FieldValue)new Quantity(2, "menages")));
        
        // set indicators to null
        instance.set(indicatorField(1).asString(), null);
        instance.set(indicatorField(2).asString(), null);

        // persist it
        assertResolves(locator.persist(instance));

        // read from server
        FormInstance secondRead = assertResolves(locator.getFormInstance(NFI_DIST_FORM_CLASS, instance.getId()));

        assertThat(secondRead.get(indicatorField(1)), nullValue()); // BENE
        assertThat(secondRead.get(indicatorField(2)), nullValue()); // BACHE

        
//        // Both BENE+BACHE and BENE/BACHE should be missing, because both
//        // BENE and BACHE are missing
//        assertEquals(null, secondRead.getValue(path(indicatorField(11))));
//        assertEquals(null, secondRead.getValue(path(indicatorField(12))));
    }

    @Test
    public void persistLocation() {

        FormInstance instance = new FormInstance(newLegacyFormInstanceId(HEALTH_CENTER_CLASS), HEALTH_CENTER_CLASS);
        instance.set(field(HEALTH_CENTER_CLASS, NAME_FIELD), "CS Ubuntu");
        instance.set(field(HEALTH_CENTER_CLASS, GEOMETRY_FIELD), new GeoPoint(-1, 13));
        instance.set(field(HEALTH_CENTER_CLASS, ADMIN_FIELD), entity(IRUMU));

        assertResolves(locator.persist(instance));

        // ensure that everything worked out
        GetLocations query = new GetLocations(getLegacyIdFromCuid(instance.getId()));
        LocationResult result = execute(query);
        LocationDTO location = result.getData().get(0);

        assertThat(location.getName(), equalTo("CS Ubuntu"));
        assertThat(location.getAdminEntity(1).getName(), equalTo("Ituri"));
        assertThat(location.getAdminEntity(2).getName(), equalTo("Irumu"));
        assertThat(location.getLatitude(), equalTo(-1d));
        assertThat(location.getLongitude(), equalTo(13d));

        // remove location
        assertResolves(locator.remove(HEALTH_CENTER_CLASS, instance.getId()));

        // check whether location is removed
        result = execute(query);
        assertThat(result.getData(), IsEmptyCollection.empty());
    }

    @Test
    public void siteDeletion() {

        QueryModel query = new QueryModel(NFI_DIST_FORM_CLASS);
        query.selectResourceId().as("id");
        query.selectField(CuidAdapter.field(VILLAGE_CLASS, CuidAdapter.NAME_FIELD));

        ColumnSet columnSet = assertResolves(locator.queryTable(query));
        assertThat(columnSet.getNumRows(), equalTo(3));

        final ResourceId firstRecordId = ResourceId.valueOf(columnSet.getColumnView("id").getString(0));

        assertResolves(locator.remove(NFI_DIST_FORM_CLASS, firstRecordId));

        columnSet = assertResolves(locator.queryTable(query));
        assertThat(columnSet.getNumRows(), equalTo(2)); // size is reduced
        assertThat(columnSet.getColumnView("id"), not(hasValue(firstRecordId)));
    }

    private Matcher<ColumnView> hasValue(final ResourceId recordId) {
        return new TypeSafeMatcher<ColumnView>() {
            @Override
            protected boolean matchesSafely(ColumnView columnView) {
                for (int i = 0; i < columnView.numRows(); i++) {
                    if(columnView.getString(i).equals(recordId.asString())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("column with ").appendValue(recordId.asString());
            }
        };
    }


    @Test
    public void updateLocation() {

//        <location locationId="1" name="Penekusu Kivu" locationTypeId="1"
//        X="1.532" Y="27.323" timeEdited="1"/>
//        <locationAdminLink locationId="1" adminEntityId="2"/>
//        <locationAdminLink locationId="1" adminEntityId="12"/>

        FormInstance instance = assertResolves(locator.getFormInstance(HEALTH_CENTER_CLASS, locationInstanceId(1)));
        instance.set(field(HEALTH_CENTER_CLASS, NAME_FIELD), "New Penekusu");

        assertResolves(locator.persist(instance));

        GetLocations query = new GetLocations(1);
        LocationResult result = execute(query);
        LocationDTO location = result.getData().get(0);

        assertThat(location.getName(), equalTo("New Penekusu"));
        assertThat(location.getLocationTypeId(), equalTo(1));
        assertThat(location.getLatitude(), equalTo(27.323));
        assertThat(location.getLongitude(), equalTo(1.532));
        assertThat(location.getAdminEntity(1).getId(), equalTo(2)); // 12 admin level is not returned because we eliminate redundant information org.activityinfo.store.mysql.side.AdminColumnBuilder.emit(int[])
    }

    @Test
    public void deleteLocation() {

        ResourceId instanceToDelete = CuidAdapter.locationInstanceId(1);
        assertResolves(locator.remove(CuidAdapter.locationFormClass(1), instanceToDelete));

        QueryModel queryModel = new QueryModel(CuidAdapter.locationFormClass(1));
        queryModel.selectResourceId().as("id");

        ColumnSet columnSet = assertResolves(locator.queryTable(queryModel));
        ColumnView idColumn = columnSet.getColumnView("id");

        for (int i = 0; i < idColumn.numRows(); i++) {
            if(idColumn.getString(i).equals(instanceToDelete.asString())) {
                throw new AssertionError();
            }
        }
    }

}
