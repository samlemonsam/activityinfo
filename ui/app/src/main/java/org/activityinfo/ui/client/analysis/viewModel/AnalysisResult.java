package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of the analysis
 */
public class AnalysisResult {

    private DimensionSet dimensionSet;
    private List<Point> points = new ArrayList<>();


    public AnalysisResult(List<MeasureResultSet> measureSets) {
        if (!measureSets.isEmpty()) {
            dimensionSet = measureSets.get(0).getDimensions();
        } else {
            dimensionSet = new DimensionSet();
        }
        for (MeasureResultSet measureSet : measureSets) {
            points.addAll(measureSet.getPoints());
        }
    }

    public DimensionSet getDimensionSet() {
        return dimensionSet;
    }

    public List<Point> getPoints() {
        return points;
    }

    public static Observable<AnalysisResult> compute(FormStore formStore, EffectiveModel effectiveModel) {

        List<Observable<MeasureResultSet>> points = new ArrayList<>();

        for (EffectiveMeasure measure : effectiveModel.getMeasures()) {
            points.add(computePoints(formStore, measure));
        }

        return Observable.flatten(points).transform(resultSets -> new AnalysisResult(resultSets));
    }

    private static Observable<MeasureResultSet> computePoints(FormStore formStore, EffectiveMeasure measure) {

        QueryModel queryModel = new QueryModel(measure.getFormId());
        queryModel.selectExpr(measure.getModel().getFormula()).as("value");

        for (EffectiveDimension dim : measure.getDimensions()) {
            queryModel.addColumns(dim.getRequiredColumns());
        }

        Observable<ColumnSet> columnSet = formStore.query(queryModel);
        Observable<MeasureResultSet> resultSet = columnSet.transform(columns -> {
            MeasureResultBuilder builder = new MeasureResultBuilder(measure, columns);
            builder.execute();
            return builder.getResult();
        });

        return resultSet;
    }


}
