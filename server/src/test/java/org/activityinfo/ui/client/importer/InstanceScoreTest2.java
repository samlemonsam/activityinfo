package org.activityinfo.ui.client.importer;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.gwt.core.client.testing.StubScheduler;
import org.activityinfo.core.server.type.converter.JvmConverterFactory;
import org.activityinfo.core.shared.importing.model.ColumnAction;
import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.core.shared.importing.model.MapExistingAction;
import org.activityinfo.core.shared.importing.source.SourceColumn;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.strategy.*;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.formTree.AsyncFormTreeBuilder;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.component.importDialog.Importer;
import org.activityinfo.ui.client.component.importDialog.data.PastedTable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.io.Resources.getResource;
import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.activityinfo.model.legacy.CuidAdapter.field;
import static org.activityinfo.model.legacy.CuidAdapter.locationFormClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by yuriyz on 9/1/2016.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/nfi-import.db.xml")
public class InstanceScoreTest2 extends CommandTestCase2 {

    private static final ResourceId ADMINISTRATIVE_UNIT_FIELD = field(locationFormClass(2), CuidAdapter.ADMIN_FIELD);

    private AsyncFormTreeBuilder formTreeBuilder;
    private StubScheduler scheduler;
    private ImportModel importModel;
    private Importer importer;
    private FormTree formTree;

    @Before
    public void before() {
        formTreeBuilder = new AsyncFormTreeBuilder(locator);
        scheduler = new StubScheduler();

        setUser(3);

        formTree = assertResolves(formTreeBuilder.apply(ImportWithMultiClassRangeTest.SCHOOL_FORM_CLASS));
        FormTreePrettyPrinter.print(formTree);

        importModel = new ImportModel(formTree);
        importer = new Importer(locator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));
    }


    @Test
    public void adminEntityScoring() throws IOException {

        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource("org/activityinfo/core/shared/importing/school-import.csv"), Charsets.UTF_8));
        importModel.setSource(source);

        AbstractImporterTest.dumpList("COLUMNS", source.getColumns());

        importModel.setColumnAction(columnIndex("School"), target("Name"));

        // Province is at the root of both hierarchies
        importModel.setColumnAction(columnIndex("Province"), target("Province Name"));

        // Admin hierarchy
        importModel.setColumnAction(columnIndex("District"), target("District Name"));
        importModel.setColumnAction(columnIndex("Territoire"), target("Territoire Name"));
        importModel.setColumnAction(columnIndex("Secteur"), target("Secteur Name"));
        importModel.setColumnAction(columnIndex("Groupement"), target("Secteur Name"));

        // health ministry hierarchy
        importModel.setColumnAction(columnIndex("Zone de Santé"), target("Zone de Santé Name"));

        FormTree.Node rootField = formTree.getRootField(ADMINISTRATIVE_UNIT_FIELD);
        TargetCollector targetCollector = new TargetCollector(rootField);

        Map<TargetSiteId, ColumnAccessor> mappedColumns = importModel.getMappedColumns(rootField.getFieldId());
        List<ColumnAccessor> sourceColumns = Lists.newArrayList();
        Map<FieldPath, Integer> referenceFields = targetCollector.getPathMap(mappedColumns, sourceColumns);

        // Province level
        ColumnSet columnSet = assertResolves(query(referenceFields, ImportWithMultiClassRangeTest.PROVINCE_LEVEL));
        InstanceScoreSource scoreSource = new InstanceScoreSourceBuilder(referenceFields, sourceColumns).build(columnSet);
        InstanceScorer.Score score = score(source.getRows().get(0), scoreSource);
        assertScore(score, "Katanga");

        // District level
        columnSet = assertResolves(query(referenceFields, ImportWithMultiClassRangeTest.DISTRICT_LEVEL));
        scoreSource = new InstanceScoreSourceBuilder(referenceFields, sourceColumns).build(columnSet);
        score = score(source.getRows().get(1), scoreSource);
        assertScore(score, "Katanga");
        assertScore(score, "Tanganika");


        // Territoire level
        columnSet = assertResolves(query(referenceFields, ImportWithMultiClassRangeTest.TERRITOIRE_LEVEL));
        scoreSource = new InstanceScoreSourceBuilder(referenceFields, sourceColumns).build(columnSet);
        score = score(source.getRows().get(2), scoreSource);
        assertScore(score, "Katanga");
        assertScore(score, "Tanganika");
        assertScore(score, "Kalemie");
        assertThat(scoreSource.getReferenceInstanceIds().get(score.getBestMatchIndex()), equalTo(ImportWithMultiClassRangeTest.TERRITOIRE_KALEMIE));
    }

    protected ColumnAction target(String debugFieldPath) {
        List<String> options = Lists.newArrayList();
        for(ImportTarget target : importer.getImportTargets()) {
            if(target.getLabel().equals(debugFieldPath)) {
                return new MapExistingAction(target);
            }
            options.add(target.getLabel());
        }
        throw new RuntimeException(String.format("No field matching '%s', we have: %s",
                debugFieldPath, options));
    }

    protected int columnIndex(String header) {
        for (SourceColumn column : importModel.getSource().getColumns()) {
            if (column.getHeader().trim().equals(header)) {
                return column.getIndex();
            }
        }
        throw new RuntimeException("No imported column with header " + header);
    }

    private Promise<ColumnSet> query(Map<FieldPath, Integer> referenceFields, int adminLevel) {
        ResourceId formId = CuidAdapter.adminLevelFormClass(adminLevel);
        QueryModel queryModel = new QueryModel(formId);
        queryModel.selectResourceId().as("_id");
        for (FieldPath fieldPath : referenceFields.keySet()) {
            queryModel.selectField(fieldPath).as(fieldPath.toString());
        }
        return locator.queryTable(queryModel);
    }

    private InstanceScorer.Score score(SourceRow row, InstanceScoreSource scoreSource) {
        return new InstanceScorer(scoreSource).score(row);
    }

    private static void assertScore(InstanceScorer.Score score, String name) {
        for (int i = 0; i < score.getImported().length; i++) {
            String imported = score.getImported()[i];
            if (name.equals(imported) && score.getBestScores()[i] >= 1.0) {
                return;
            }
        }
        throw new RuntimeException("Failed to score : " + name);
    }
}
