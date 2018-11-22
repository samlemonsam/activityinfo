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
package org.activityinfo.ui.client.component.importDialog.model.strategy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.hierarchy.Hierarchy;
import org.activityinfo.ui.client.component.form.field.hierarchy.Level;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidationResult;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import java.util.*;

/**
 * @author yuriyz on 5/19/14.
 */
public class HierarchyClassImporter implements FieldImporter {

    private final FormTree.Node rootField;
    private final Map<FieldPath, Integer> referenceFields;
    private final List<ColumnAccessor> sourceColumns;
    private final List<FieldImporterColumn> fieldImporterColumns;
    private final Map<ResourceId, InstanceScoreSource> scoreSources = Maps.newHashMap();
    private final Hierarchy hierarchy;
    private final ArrayList<Level> levels;

    public HierarchyClassImporter(FormTree.Node rootField,
                                  List<ColumnAccessor> sourceColumns,
                                  Map<FieldPath, Integer> referenceFields,
                                  List<FieldImporterColumn> fieldImporterColumns) {
        this.rootField = rootField;
        this.sourceColumns = sourceColumns;
        this.referenceFields = referenceFields;
        this.fieldImporterColumns = fieldImporterColumns;
        this.hierarchy = Hierarchy.get(rootField);
        this.levels = Lists.newArrayList(hierarchy.getLevels());
        Collections.reverse(this.levels);
    }

    @Override
    public Promise<Void> prepare(ResourceLocator locator, List<? extends SourceRow> batch) {
        final List<Promise<Void>> promises = Lists.newArrayList();

        for (final ResourceId formId : rootField.getRange()) {
            if(isMatchableAtLevel(formId, referenceFields.keySet())) {

                QueryModel queryModel = new QueryModel(formId);
                queryModel.selectRecordId().as("_id");

                for (FieldPath referenceFieldPath : referenceFields.keySet()) {
                    queryModel.selectField(referenceFieldPath).as(referenceFieldPath.toString());
                }
                promises.add(locator.queryTable(queryModel).then(new Function<ColumnSet, Void>() {
                    @Override
                    public Void apply(ColumnSet columnSet) {
                        scoreSources.put(formId, new InstanceScoreSourceBuilder(formId, referenceFields, sourceColumns).build(columnSet));
                        return null;
                    }
                }));
            }
        }

        return Promise.waitAll(promises);
    }

    /**
     * To be "matchable" at a given level, we must have at least one field on the form itself.
     *
     *
     * <p>For example, suppose you have a field that can reference EITHER a Province or a Territory, and Territory
     * is a child level of Province. If you have a Territory Name or Code, then you could plausibly match against
     * the territories. However, if you only have a Province name, we have to limit our matches to province.</p>
     */
    private boolean isMatchableAtLevel(ResourceId formId, Set<FieldPath> referenceFields) {

        // TODO: these reference fields should really be qualified with the
        // the formId. This will only work with admin levels
        String prefix = formId.asString();

        for (FieldPath referenceField : referenceFields) {
            if(referenceField.isRoot() && referenceField.toString().startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    private FieldImporterColumn getImportedColumn(ColumnAccessor columnAccessor) {
        for (FieldImporterColumn c : fieldImporterColumns) {
            if (c.getAccessor().equals(columnAccessor)) {
                return c;
            }
        }
        return null; // bug?
    }

    @Override
    public void validateInstance(SourceRow row, List<ValidationResult> results) {

        // Start from the leaf level and work our way up to the parent levels
        for (Level level : levels) {
            InstanceScoreSource scoreSource = scoreSources.get(level.getFormId());
            if(scoreSource != null) {

                InstanceScorer instanceScorer = new InstanceScorer(scoreSource);
                final InstanceScorer.Score score = instanceScorer.score(row);
                final int bestMatchIndex = score.getBestMatchIndex();

                if(bestMatchIndex != -1) {
                    for (int i = 0; i < sourceColumns.size(); i++) {
                        String matched = scoreSource.getReferenceValues().get(bestMatchIndex)[i];
                        final ValidationResult converted = ValidationResult.converted(matched, score.getBestScores()[i]);
                        converted.setRef(new RecordRef(level.getFormId(), scoreSource.getReferenceInstanceIds().get(bestMatchIndex)));
                        results.add(converted);
                    }
                    return;
                }
            }
        }
        for (int i = 0; i < sourceColumns.size(); i++) {
            results.add(ValidationResult.error("No match"));
        }
    }

    @Override
    public boolean updateInstance(SourceRow row, TypedFormRecord instance) {
        final List<ValidationResult> validationResults = Lists.newArrayList();
        validateInstance(row, validationResults);

        final Map<ResourceId, RecordRef> toSave = Maps.newHashMap();
        for (ValidationResult result : validationResults) {
            if (result.isPersistable() && result.getRef() != null) {
                ResourceId range = result.getRef().getFormId();
                RecordRef value = toSave.get(range);
                if (value == null) {
                    toSave.put(range, result.getRef());
                }
            }
        }
        if (!toSave.isEmpty()) {
            instance.set(rootField.getFieldId(), new ReferenceValue(toSave.values()));
            return true;
        }

        return false;
    }

    @Override
    public List<FieldImporterColumn> getColumns() {
        return fieldImporterColumns;
    }
}
