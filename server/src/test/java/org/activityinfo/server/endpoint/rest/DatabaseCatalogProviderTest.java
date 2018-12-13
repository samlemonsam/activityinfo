package org.activityinfo.server.endpoint.rest;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.DatabaseModule;
import org.activityinfo.server.database.OnDataSet;

import org.activityinfo.store.spi.FormCatalog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class,
        DatabaseModule.class
})
@OnDataSet("/dbunit/schema4.db.xml")
public class DatabaseCatalogProviderTest {

    @Inject
    private FormCatalog catalog;

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private static final int IRAQ_DB = 1;
    private static final int SYRIA_DB = 2;
    private static final int LEBANON_DB = 3;

    private static final int ALEX = 1;
    private static final int BAVON = 2;
    private static final int JOHN = 3;
    private static final int JACOB = 4;

    private static final ResourceId INTAKE_PARENT_FORM = CuidAdapter.activityFormClass(9);
    private static final ResourceId INTAKE_SUB_FORM = ResourceId.valueOf("cjpcn95rd1");

    private static final String RDC_CODE = GeoDatabaseProvider.COUNTRY_ID_PREFIX + "CD";
    private static final String RDC_LABEL = "Rdc";

    // Admin Level Forms
    private static final int PROVINCE = 1;
    private static final String PROVINCE_CODE = CuidAdapter.adminLevelFormClass(PROVINCE).asString();
    private static final String PROVINCE_LABEL = "Province";

    // Location Type Forms
    private static final int LOCALITIE = 1;
    private static final String LOCALITIE_CODE = CuidAdapter.locationFormClass(LOCALITIE).asString();
    private static final String LOCALITIE_LABEL = "Localitie";

    private static final int COUNTRY = 2;
    private static final String COUNTRY_CODE = CuidAdapter.locationFormClass(COUNTRY).asString();
    private static final String COUNTRY_LABEL = "Country";


    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() throws IOException {
        helper.tearDown();
    }

    @Test
    public void rootResources() {
        // Should have two entries: geodb and databases
        List<CatalogEntry> rootEntries = catalog.getRootEntries();
        assert rootEntries.size() == 2;
        assert rootEntries.get(0).getId().equals(GeoDatabaseProvider.GEODB_ID.asString());
        assert rootEntries.get(1).getId().equals(UserDatabaseProvider.ROOT_ID);
    }

    @Test
    public void countries() {
        // should have only one country on current data set : Rdc
        List<CatalogEntry> countries = catalog.getChildren(GeoDatabaseProvider.GEODB_ID.asString(), ALEX);
        assert countries.size() == 1;
        assert countries.get(0).getId().equals(RDC_CODE);
        assert countries.get(0).getLabel().equals(RDC_LABEL);
        assert countries.get(0).getType().equals(CatalogEntryType.FOLDER);
        assert !countries.get(0).isLeaf();
    }

    @Test
    public void countryForms() {
        List<CatalogEntry> countryForms = catalog.getChildren(RDC_CODE, ALEX);

        List<CatalogEntry> adminLevelForms = countryForms.stream()
                .filter(entry -> entry.getId().charAt(0) == CuidAdapter.ADMIN_LEVEL_DOMAIN)
                .collect(Collectors.toList());
        List<String> expectedEntries = Lists.newArrayList(PROVINCE_CODE);
        matchEntries(adminLevelForms, expectedEntries, CatalogEntryType.FORM, true);

        List<CatalogEntry> locationTypeForms = countryForms.stream()
                .filter(entry -> entry.getId().charAt(0) == CuidAdapter.LOCATION_TYPE_DOMAIN)
                .collect(Collectors.toList());
        expectedEntries = Lists.newArrayList(LOCALITIE_CODE, COUNTRY_CODE);
        matchEntries(locationTypeForms, expectedEntries, CatalogEntryType.FORM, true);
    }

    @Test
    public void databases_alex() {
        // should have 2 databases for alex
        List<CatalogEntry> databases = catalog.getChildren(UserDatabaseProvider.ROOT_ID, ALEX);
        List<String> expectedEntries = Lists.newArrayList(
                CuidAdapter.databaseId(IRAQ_DB).asString(),
                CuidAdapter.databaseId(SYRIA_DB).asString());
        matchEntries(databases, expectedEntries, CatalogEntryType.FOLDER, false);

        // IRAQ Database
        List<CatalogEntry> iraqFolders = catalog.getChildren(CuidAdapter.databaseId(IRAQ_DB).asString(), ALEX);
        expectedEntries = Lists.newArrayList(
                CuidAdapter.folderId(1).asString(),
                CuidAdapter.folderId(2).asString(),
                CuidAdapter.folderId(3).asString());
        matchEntries(iraqFolders, expectedEntries, CatalogEntryType.FOLDER, false);

        List<CatalogEntry> folderForms = catalog.getChildren(CuidAdapter.folderId(1).asString(), ALEX);
        expectedEntries = Lists.newArrayList(
                CuidAdapter.activityFormClass(1).asString(),
                CuidAdapter.activityFormClass(2).asString());
        matchEntries(folderForms, expectedEntries, CatalogEntryType.FORM, true);
    }

    private void matchEntries(List<CatalogEntry> fetched, List<String> expectedEntries, CatalogEntryType expectedType, boolean leaf) {
        assert fetched.size() == expectedEntries.size();
        for (CatalogEntry entry : fetched) {
            assert expectedEntries.contains(entry.getId());
            assert entry.getType().equals(expectedType);
            assert entry.isLeaf() == leaf;
        }
    }

    @Test
    public void databases_bavon() {
        // should have 1 database for bavon
        List<CatalogEntry> databases = catalog.getChildren(UserDatabaseProvider.ROOT_ID, BAVON);
        List<String> expectedEntries = Lists.newArrayList(CuidAdapter.databaseId(LEBANON_DB).asString());
        matchEntries(databases, expectedEntries, CatalogEntryType.FOLDER, false);

        // LEBANON Database
        List<CatalogEntry> lebanonResources = catalog.getChildren(CuidAdapter.databaseId(LEBANON_DB).asString(), BAVON);

        List<CatalogEntry> folders = lebanonResources.stream().filter(entry -> entry.getType().equals(CatalogEntryType.FOLDER)).collect(Collectors.toList());
        expectedEntries = Lists.newArrayList(
                CuidAdapter.folderId(7).asString(),
                CuidAdapter.folderId(8).asString());
        matchEntries(folders, expectedEntries, CatalogEntryType.FOLDER, false);

        // Intake Form has a Sub-Form, so shouldn't be a leaf
        List<CatalogEntry> forms = lebanonResources.stream().filter(entry -> entry.getType().equals(CatalogEntryType.FORM)).collect(Collectors.toList());
        expectedEntries = Lists.newArrayList(INTAKE_PARENT_FORM.asString());
        matchEntries(forms, expectedEntries, CatalogEntryType.FORM, false);

        List<CatalogEntry> intakeFormResources = catalog.getChildren(INTAKE_PARENT_FORM.asString(), BAVON);
        expectedEntries = Lists.newArrayList(INTAKE_SUB_FORM.asString());
        matchEntries(intakeFormResources, expectedEntries, CatalogEntryType.FORM, true);
    }

    @Test
    public void databases_john() {
        // should have 2 databases for john
        List<CatalogEntry> databases = catalog.getChildren(UserDatabaseProvider.ROOT_ID, JOHN);
        List<String> expectedEntries = Lists.newArrayList(
                CuidAdapter.databaseId(SYRIA_DB).asString(),
                CuidAdapter.databaseId(LEBANON_DB).asString());
        matchEntries(databases, expectedEntries, CatalogEntryType.FOLDER, false);

        // On SYRIA database, John should only be able to see folder 4 and its contained resources
        List<CatalogEntry> syriaFolders = catalog.getChildren(CuidAdapter.databaseId(SYRIA_DB).asString(), JOHN);
        expectedEntries = Lists.newArrayList(CuidAdapter.folderId(4).asString());
        matchEntries(syriaFolders, expectedEntries, CatalogEntryType.FOLDER, false);

        List<CatalogEntry> syriaFolderForms = catalog.getChildren(CuidAdapter.folderId(4).asString(), JOHN);
        expectedEntries = Lists.newArrayList(
                CuidAdapter.activityFormClass(5).asString(),
                CuidAdapter.activityFormClass(6).asString());
        matchEntries(syriaFolderForms, expectedEntries, CatalogEntryType.FORM, true);

        // On LEBANON database, John should have assigned permissions to view folder 7
        // but should also be able to see Form 9 as it is published
        List<CatalogEntry> lebanonResources = catalog.getChildren(CuidAdapter.databaseId(LEBANON_DB).asString(), JOHN);

        // Published form should be visible (but is not leaf as it has a subform)
        List<CatalogEntry> lebanonForms = lebanonResources.stream().filter(entry -> entry.getType().equals(CatalogEntryType.FORM)).collect(Collectors.toList());
        expectedEntries = Lists.newArrayList(INTAKE_PARENT_FORM.asString());
        matchEntries(lebanonForms, expectedEntries, CatalogEntryType.FORM, false);

        // Folder 7 should be only one visible
        List<CatalogEntry> lebanonFolders = lebanonResources.stream().filter(entry -> entry.getType().equals(CatalogEntryType.FOLDER)).collect(Collectors.toList());
        expectedEntries = Lists.newArrayList(CuidAdapter.folderId(7).asString());
        matchEntries(lebanonFolders, expectedEntries, CatalogEntryType.FOLDER, false);
    }

    @Test
    public void databases_jacob() {
        // should have 0 databases for jacob
        List<CatalogEntry> databases = catalog.getChildren(UserDatabaseProvider.ROOT_ID, JACOB);
        assert databases.size() == 0;
    }

}