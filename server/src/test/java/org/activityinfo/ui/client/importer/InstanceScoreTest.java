package org.activityinfo.ui.client.importer;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.server.type.converter.JvmConverterFactory;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.core.shared.form.tree.FormTreePrettyPrinter;
import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.strategy.*;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.promise.Promise;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.component.importDialog.Importer;
import org.activityinfo.ui.client.component.importDialog.data.PastedTable;
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
 * @author yuriyz on 5/20/14.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/nfi-import.db.xml")
public class InstanceScoreTest extends AbstractImporterTest {

    private static final ResourceId ADMINISTRATIVE_UNIT_FIELD = field(locationFormClass(2), CuidAdapter.ADMIN_FIELD);

    @Test
    public void adminEntityScoring() throws IOException {
        setUser(3);

        FormTree formTree = assertResolves(formTreeBuilder.apply(ImportWithMultiClassRangeTest.SCHOOL_FORM_CLASS));
        FormTreePrettyPrinter.print(formTree);

        importModel = new ImportModel(formTree);
        importer = new Importer(resourceLocator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));


        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource("org/activityinfo/core/shared/importing/school-import.csv"), Charsets.UTF_8));
        importModel.setSource(source);

        dumpList("COLUMNS", source.getColumns());

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
        List<Projection> projections = assertResolves(query(referenceFields, ImportWithMultiClassRangeTest.PROVINCE_LEVEL));
        InstanceScoreSource scoreSource = new InstanceScoreSourceBuilder(referenceFields, sourceColumns).build(projections);
        InstanceScorer.Score score = score(source.getRows().get(0), scoreSource);
        assertScore(score, "Katanga");

        // District level
        projections = assertResolves(query(referenceFields, ImportWithMultiClassRangeTest.DISTRICT_LEVEL));
        scoreSource = new InstanceScoreSourceBuilder(referenceFields, sourceColumns).build(projections);
        score = score(source.getRows().get(1), scoreSource);
        assertScore(score, "Katanga");
        assertScore(score, "Tanganika");


        // Territoire level
        projections = assertResolves(query(referenceFields, ImportWithMultiClassRangeTest.TERRITOIRE_LEVEL));
        scoreSource = new InstanceScoreSourceBuilder(referenceFields, sourceColumns).build(projections);
        score = score(source.getRows().get(2), scoreSource);
        assertScore(score, "Katanga");
        assertScore(score, "Tanganika");
        assertScore(score, "Kalemie");
        assertThat(scoreSource.getReferenceInstanceIds().get(score.getBestMatchIndex()), equalTo(ImportWithMultiClassRangeTest.TERRITOIRE_KALEMIE));

    }

    private Promise<List<Projection>> query(Map<FieldPath, Integer> referenceFields, int adminLevel) {
        ResourceId range = CuidAdapter.adminLevelFormClass(adminLevel);
        return resourceLocator.query(new InstanceQuery(Lists.newArrayList(referenceFields.keySet()), new ClassCriteria(range)));
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
