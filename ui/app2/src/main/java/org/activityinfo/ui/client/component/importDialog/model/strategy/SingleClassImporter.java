package org.activityinfo.ui.client.component.importDialog.model.strategy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidationResult;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;


public class SingleClassImporter implements FieldImporter {

    private ResourceId rangeFormId;
    private ResourceId fieldId;

    private boolean required;
    /**
     * List of columns to match against name properties of potential reference matches.
     */
    private List<ColumnAccessor> sources;

    private List<FieldImporterColumn> fieldImporterColumns = Lists.newArrayList();

    /**
     * The list of nested text fields to match against, mapped to the
     * index of the column they are to be matched against.
     */
    private Map<FieldPath, Integer> referenceFields;

    private InstanceScoreSource scoreSource;
    private InstanceScorer instanceScorer = null;

    public SingleClassImporter(ResourceId rangeFormId,
                               boolean required,
                               List<ColumnAccessor> sourceColumns,
                               Map<FieldPath, Integer> referenceFields,
                               List<FieldImporterColumn> fieldImporterColumns,
                               ResourceId fieldId) {
        this.rangeFormId = rangeFormId;
        this.required = required;
        this.sources = sourceColumns;
        this.referenceFields = referenceFields;
        this.fieldImporterColumns = fieldImporterColumns;
        this.fieldId = fieldId;
    }

    @Override
    public Promise<Void> prepare(ResourceLocator locator, List<? extends SourceRow> batch) {

        QueryModel queryModel = new QueryModel(rangeFormId);
        queryModel.selectResourceId().as("_id");
        for (FieldPath fieldPath : referenceFields.keySet()) {
            queryModel.selectField(fieldPath);
        }

        return locator.queryTable(queryModel).then(new Function<ColumnSet, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ColumnSet input) {
                scoreSource = new InstanceScoreSourceBuilder(rangeFormId, referenceFields, sources).build(input);
                instanceScorer = new InstanceScorer(scoreSource);
                return null;
            }
        });
    }
    
    public static String[] toArray(ColumnSet columnSet, int row, Map<FieldPath, Integer> referenceFields, int size) {
        String[] values = new String[size];
        for (Map.Entry<FieldPath, Integer> entry : referenceFields.entrySet()) {
            ColumnView columnView = columnSet.getColumnView(entry.getKey().toString());
            if(columnView != null) {
                String stringValue = columnView.getString(row);
                if (stringValue != null) {
                    values[entry.getValue()] = stringValue;
                }
            }
        }
        return values;
    }

    @Override
    public void validateInstance(SourceRow row, List<ValidationResult> results) {
        final InstanceScorer.Score score = instanceScorer.score(row);
        final int bestMatchIndex = score.getBestMatchIndex();

        ResourceId instanceId = bestMatchIndex != -1 ? scoreSource.getReferenceInstanceIds().get(bestMatchIndex) : null;

        for (int i = 0; i != sources.size(); ++i) {
            if (score.getImported()[i] == null) {
                if(required) {
                    results.add(ValidationResult.error("required missing").setRef(new RecordRef(rangeFormId, instanceId)));
                } else {
                    results.add(ValidationResult.missing().setRef(new RecordRef(rangeFormId, instanceId)));
                }
            } else if (bestMatchIndex == -1) {
                results.add(ValidationResult.error("No match"));
            } else {
                String matched = scoreSource.getReferenceValues().get(bestMatchIndex)[i];
                results.add(ValidationResult.converted(matched, score.getBestScores()[i])
                        .setRef(new RecordRef(rangeFormId, instanceId)));
            }
        }
    }

    @Override
    public boolean updateInstance(SourceRow row, FormInstance instance) {

        final List<ValidationResult> validationResults = Lists.newArrayList();
        validateInstance(row, validationResults);
        for (ValidationResult result : validationResults) {
            if (result.isPersistable() && result.getRef() != null) {
                instance.set(fieldId, new ReferenceValue(result.getRef()));
                break;
            }
        }

        return true;
    }

    @Override
    public List<FieldImporterColumn> getColumns() {
        return fieldImporterColumns;
    }

}
