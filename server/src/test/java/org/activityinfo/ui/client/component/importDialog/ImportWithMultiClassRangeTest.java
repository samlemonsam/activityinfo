package org.activityinfo.ui.client.component.importDialog;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.SortInfo;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.match.JvmConverterFactory;
import org.activityinfo.ui.client.component.importDialog.model.source.PastedTable;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImportStrategies;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.io.Resources.getResource;
import static org.activityinfo.promise.PromiseMatchers.assertResolves;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/nfi-import.db.xml")
public class ImportWithMultiClassRangeTest extends AbstractImporterTest {

    public static final ResourceId NFI_DISTRIBUTION_FORM_CLASS = CuidAdapter.activityFormClass(33);

    public static final ResourceId SCHOOL_FORM_CLASS = CuidAdapter.locationFormClass(2);

    public static final ResourceId ADMIN_FIELD = CuidAdapter.field(SCHOOL_FORM_CLASS, CuidAdapter.ADMIN_FIELD);


    // admin levels
    public static final int PROVINCE_LEVEL = 1;
    public static final int DISTRICT_LEVEL = 2;
    public static final int TERRITOIRE_LEVEL = 3;
    public static final int SECTEUR_LEVEL = 4;
    public static final int GROUPEMENT_LEVEL = 5;
    public static final int ZONE_DE_SANTE = 7;
    public static final int AIRE_DE_SANTE = 8;

    // indicators
    public static final int NUMBER_MENAGES = 118;

    // attributes
    public static final int ECHO = 400;
    public static final int DEPLACEMENT = 63;

    public static final ResourceId PROVINCE_KATANGA = CuidAdapter.entity(141804);
    public static final ResourceId DISTRICT_TANGANIKA = CuidAdapter.entity(141845);
    public static final ResourceId TERRITOIRE_KALEMIE = CuidAdapter.entity(141979);
    public static final ResourceId SECTEUR_TUMBWE = CuidAdapter.entity(142803);
    public static final ResourceId GROUPEMENT_LAMBO_KATENGA = CuidAdapter.entity(148235);
    public static final ResourceId ZONE_SANTE_NYEMBA = CuidAdapter.entity(212931);
    private ColumnSet resultSet;


    @Test
    public void testSimple() throws IOException {

        setUser(3);

        FormTree formTree = assertResolves(formTreeBuilder.apply(NFI_DISTRIBUTION_FORM_CLASS));
        FormTreePrettyPrinter.print(formTree);

        importModel = new ImportModel(formTree);
        importer = new Importer(locator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));

        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource(getClass(), "nfi.csv"), Charsets.UTF_8));
        source.parseAllRows();

        importModel.setSource(source);

        dumpList("COLUMNS", source.getColumns());
        importModel.setColumnAction(columnIndex("Date1"), target("Start Date"));
        importModel.setColumnAction(columnIndex("Date2"), target("End Date"));
        importModel.setColumnAction(columnIndex("Partner"), target("Partner Name"));
        importModel.setColumnAction(columnIndex("Localité"), target("Localité Name"));
        importModel.setColumnAction(columnIndex("Province"), target("Province Name"));
        importModel.setColumnAction(columnIndex("District"), target("District Name"));
        importModel.setColumnAction(columnIndex("Territoire"), target("Territoire Name"));
        importModel.setColumnAction(columnIndex("Secteur"), target("Secteur Name"));
        importModel.setColumnAction(columnIndex("Groupement"), target("Groupement Name"));
        importModel.setColumnAction(columnIndex("Zone de Santé"), target("Zone de Santé Name"));
        importModel.setColumnAction(columnIndex("Nombre de ménages ayant reçu une assistance en NFI"),
                target("Nombre de ménages ayant reçu une assistance en NFI"));

        ValidatedRowTable validatedResult = assertResolves(importer.validateRows(importModel));
        showValidationGrid(validatedResult);

        assertResolves(importer.persist(importModel));

        GetSites query = new GetSites(Filter.filter().onActivity(33));
        query.setSortInfo(new SortInfo("date2", Style.SortDir.DESC));

        SiteResult result = execute(query);
        assertThat(result.getTotalLength(), equalTo(643)); // 651 - 8 = 643 (8 records where start date is before end date)
//        assertThat(result.getTotalLength(), equalTo(313));

        SiteDTO lastSite = result.getData().get(0);
//        assertThat(lastSite.getDate2(), equalTo(new LocalDate(2013,4,26)));
        assertThat(lastSite.getDate2(), equalTo(new LocalDate(2013,4,30)));
        assertThat(lastSite.getLocationName(), equalTo("Kilimani Camp"));
        assertThat(lastSite.getAdminEntity(PROVINCE_LEVEL).getName(), equalTo("Nord Kivu"));
        assertThat(lastSite.getAdminEntity(DISTRICT_LEVEL).getName(), equalTo("Nord Kivu"));
        assertThat(lastSite.getAdminEntity(TERRITOIRE_LEVEL).getName(), equalTo("Masisi"));
        assertThat(lastSite.getAdminEntity(SECTEUR_LEVEL).getName(), equalTo("Masisi"));

        assertThat((Double) lastSite.getIndicatorValue(NUMBER_MENAGES), equalTo(348.0));
        assertThat(lastSite.getAttributeValue(ECHO), equalTo(false));
    }

    @Test
    public void testMulti() throws IOException {

        setUser(3);

        FormTree formTree = assertResolves(formTreeBuilder.apply(SCHOOL_FORM_CLASS));
        FormTreePrettyPrinter.print(formTree);

        importModel = new ImportModel(formTree);
        importer = new Importer(locator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));


        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource(getClass(), "school-import.csv"), Charsets.UTF_8));
        importModel.setSource(source);

        dumpList("COLUMNS", source.getColumns());

        importModel.setColumnAction(columnIndex("School"), target("Name"));

        // Province is at the root of both hierarchies
        importModel.setColumnAction(columnIndex("Province"), target("Province Name"));

        // Admin hierarchy
        importModel.setColumnAction(columnIndex("District"), target("District Name"));
        importModel.setColumnAction(columnIndex("Territoire"), target("Territoire Name"));
        importModel.setColumnAction(columnIndex("Secteur"), target("Secteur Name"));
        importModel.setColumnAction(columnIndex("Groupement"), target("Groupement Name"));

        // health ministry hierarchy
        importModel.setColumnAction(columnIndex("Zone de Santé"), target("Zone de Santé Name"));

        ValidatedRowTable validatedResult = assertResolves(importer.validateRows(importModel));
        showValidationGrid(validatedResult);

        assertResolves(importer.persist(importModel));

        QueryModel resultQuery = new QueryModel(SCHOOL_FORM_CLASS);
        resultQuery.selectResourceId().as("id");
        resultQuery.selectField(CuidAdapter.field(SCHOOL_FORM_CLASS, CuidAdapter.NAME_FIELD)).as("name");

        resultSet = assertResolves(locator.queryTable(resultQuery));
        assertThat(resultSet.getNumRows(), equalTo(8)); // we have 8 rows in school-import.csv
        
        assertThat(school("P"), equalTo(set(PROVINCE_KATANGA)));
        assertThat(school("D"), equalTo(set(DISTRICT_TANGANIKA)));
        assertThat(school("T"), equalTo(set(TERRITOIRE_KALEMIE)));
        assertThat(school("S"), equalTo(set(SECTEUR_TUMBWE)));
        assertThat(school("G"), equalTo(set(GROUPEMENT_LAMBO_KATENGA)));
        assertThat(school("GZ"), equalTo(set(GROUPEMENT_LAMBO_KATENGA, ZONE_SANTE_NYEMBA)));
        assertThat(school("TZ"), equalTo(set(TERRITOIRE_KALEMIE, ZONE_SANTE_NYEMBA)));
    }

    private Set<ResourceId> school(String name) {
        
        // Find id of the school with this name
        ResourceId id = null;
        ColumnView nameColumn = resultSet.getColumnView("name");
        ColumnView idColumn = resultSet.getColumnView("id");
        
        for (int i = 0; i < resultSet.getNumRows(); i++) {
            if (name.equals(nameColumn.getString(i))) {
                id = ResourceId.valueOf(idColumn.getString(i));
                break;
            }    
        }
        if(id == null) {
            throw new AssertionError("No school named '" + name + "'");
        }

        Promise<FormInstance> record = locator.getFormInstance(SCHOOL_FORM_CLASS, id);
        ReferenceValue value = (ReferenceValue) record.get().get(CuidAdapter.field(SCHOOL_FORM_CLASS, CuidAdapter.ADMIN_FIELD));

        Set<ResourceId> recordIds = new HashSet<>();
        for (RecordRef recordRef : value.getReferences()) {
            recordIds.add(recordRef.getRecordId());
        }

        return recordIds;
    }

    public static Set<ResourceId> set(ResourceId... resourceIds) {
        return Sets.newHashSet(resourceIds);
    }
}
