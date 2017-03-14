package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.model.expr.functions.SumFunction;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.Aggregation;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
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

    public static Observable<AnalysisResult> compute(FormStore formStore, AnalysisModel model, FormForest formForest) {

        DimensionSet dimensionSet = new DimensionSet(model.getDimensions());
        List<Observable<MeasureResultSet>> points = new ArrayList<>();

        for (MeasureModel measureModel : model.getMeasures()) {
            points.add(computePoints(formStore, formForest, measureModel, dimensionSet));
        }

        return Observable.flatten(points).transform(resultSets -> new AnalysisResult(resultSets));
    }

    private static Observable<MeasureResultSet> computePoints(FormStore formStore, FormForest formForest, MeasureModel measureModel, DimensionSet dimensionSet) {
//
//        FormTree formTree = formForest.findTree(measureModel.getFormId());
//
//        for (DimensionModel dimensionModel : dimensionSet) {
//
//        }

        QueryModel queryModel = new QueryModel(measureModel.getFormId());
        queryModel.selectExpr(measureModel.getFormula()).as("value");

        Observable<ColumnSet> columnSet = formStore.query(queryModel);
        Observable<MeasureResultSet> resultSet = columnSet.transform(columns -> {

            ColumnView value = columns.getColumnView("value");
            GroupMap groupMap = new GroupMap(dimensionSet, columns);

            // Build group/value pairs
            int numRows = columns.getNumRows();
            double valueArray[] = new double[numRows];
            int groupArray[] = new int[numRows];
            for (int i = 0; i < numRows; i++) {
                valueArray[i] = value.getDouble(i);
                groupArray[i] = groupMap.groupAt(i);
            }

            // Aggregate into groups
            int numGroups = groupMap.getGroupCount();
            double aggregatedValues[] = Aggregation.aggregate(SumFunction.INSTANCE,
                    groupArray,
                    valueArray,
                    numRows,
                    numGroups);

            // Add the points
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < numGroups; i++) {
                points.add(new Point(groupMap.getGroup(i), aggregatedValues[i]));
            }

            return new MeasureResultSet(dimensionSet, points);
        });

        return resultSet;
    }
}