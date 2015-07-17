package org.activityinfo.core.shared.importing.strategy;
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.Pair;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.FormClassSet;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.validation.ValidationResult;
import org.activityinfo.promise.Promise;
import org.activityinfo.model.legacy.CuidAdapter;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 5/19/14.
 */
public class HierarchyClassImporter implements FieldImporter {

    private final FormTree.Node rootField;
    private final Map<FieldPath, Integer> referenceFields;
    private final List<ColumnAccessor> sourceColumns;
    private final List<FieldImporterColumn> fieldImporterColumns;
    private final Map<ResourceId, InstanceScoreSource> scoreSources = Maps.newHashMap();

    public HierarchyClassImporter(FormTree.Node rootField,
                                  List<ColumnAccessor> sourceColumns,
                                  Map<FieldPath, Integer> referenceFields,
                                  List<FieldImporterColumn> fieldImporterColumns) {
        this.rootField = rootField;
        this.sourceColumns = sourceColumns;
        this.referenceFields = referenceFields;
        this.fieldImporterColumns = fieldImporterColumns;
    }

    @Override
    public Promise<Void> prepare(ResourceLocator locator, List<? extends SourceRow> batch) {
        final List<Promise<Void>> promises = Lists.newArrayList();
        for (final ResourceId range : FormClassSet.of(rootField.getRange()).getElements()) {
            InstanceQuery query = new InstanceQuery(Lists.newArrayList(referenceFields.keySet()), new ClassCriteria(range));
            final Promise<List<Projection>> promise = locator.query(query);
            promise.then(new Function<List<Projection>, Void>() {
                @Override
                public Void apply(List<Projection> projections) {
                    scoreSources.put(range, new InstanceScoreSourceBuilder(referenceFields, sourceColumns).build(projections));
                    return null;
                }
            });
        }

        return Promise.waitAll(promises);
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
        for (int i = 0; i < sourceColumns.size(); i++) {
            ColumnAccessor columnAccessor = sourceColumns.get(i);
            if (!columnAccessor.isMissing(row)) {
                FieldImporterColumn importedColumn = getImportedColumn(columnAccessor);
                ResourceId targetSiteId = ResourceId.valueOf(importedColumn.getTarget().getSite().asString());
                if (targetSiteId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN) {
                    final int levelId = CuidAdapter.getBlock(targetSiteId, 0);
                    // todo : recreation of admin level cuid seems to be error prone, check later !
                    ResourceId range = CuidAdapter.adminLevelFormClass(levelId);
                    InstanceScoreSource scoreSource = scoreSources.get(range);
                    InstanceScorer instanceScorer = new InstanceScorer(scoreSource);
                    final InstanceScorer.Score score = instanceScorer.score(row);
                    final int bestMatchIndex = score.getBestMatchIndex();

                    if (score.getImported()[i] == null) {
                        results.add(ValidationResult.MISSING);
                    } else if (bestMatchIndex == -1) {
                        results.add(ValidationResult.error("No match"));
                    } else {
                        String matched = scoreSource.getReferenceValues().get(bestMatchIndex)[i];
                        final ValidationResult converted = ValidationResult.converted(matched, score.getBestScores()[i]);
                        converted.setRangeWithInstanceId(new Pair<>(range, scoreSource.getReferenceInstanceIds().get(bestMatchIndex)));
                        results.add(converted);
                    }
                } else {
                    throw new UnsupportedOperationException("Not supported");
                }
            } else {
                results.add(ValidationResult.MISSING);
            }
        }

    }

    @Override
    public boolean updateInstance(SourceRow row, FormInstance instance) {
        final List<ValidationResult> validationResults = Lists.newArrayList();
        validateInstance(row, validationResults);

        final Map<ResourceId, ResourceId> toSave = Maps.newHashMap();
        for (ValidationResult result : validationResults) {
            if (result.shouldPersist() && result.getRangeWithInstanceId() != null) {
                ResourceId range = result.getRangeWithInstanceId().getA();
                ResourceId value = toSave.get(range);
                if (value == null) {
                    toSave.put(range, result.getRangeWithInstanceId().getB());
                }
            }
        }
        if (!toSave.isEmpty()) {
            instance.set(rootField.getFieldId(), Sets.newHashSet(toSave.values()));
            return true;
        }

        return false;
    }

    @Override
    public List<FieldImporterColumn> getColumns() {
        return fieldImporterColumns;
    }
}
