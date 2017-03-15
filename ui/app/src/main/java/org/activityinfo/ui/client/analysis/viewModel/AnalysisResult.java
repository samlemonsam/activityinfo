package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.expr.functions.ExprFunctions;
import org.activityinfo.model.expr.functions.StatFunction;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.Aggregation;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.DimensionMapping;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.Collections;
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

        QueryModel queryModel = new QueryModel(measureModel.getFormId());
        queryModel.selectExpr(measureModel.getFormula()).as("value");


        List<DimensionReaderFactory> readers = new ArrayList<>();

        for (int i = 0; i < dimensionSet.getCount(); i++) {
            DimensionModel dimension = dimensionSet.getDimension(i);
            DimensionMapping mapping = findMapping(dimension, measureModel);
            if(mapping != null) {
                String columnId = "d" + i;
                queryModel.selectExpr(mapping.getFormula()).as(columnId);
                readers.add( columnSet -> new ColumnReader(columnSet.getColumnView(columnId)) );
            }
        }

        Observable<ColumnSet> columnSet = formStore.query(queryModel);
        Observable<MeasureResultSet> resultSet = columnSet.transform(columns -> {

            ColumnView value = columns.getColumnView("value");
            GroupMap groupMap = new GroupMap(dimensionSet, columns, readers);

            // Build group/value pairs
            int numRows = columns.getNumRows();
            double valueArray[] = new double[numRows];
            int groupArray[] = new int[numRows];
            for (int i = 0; i < numRows; i++) {
                valueArray[i] = value.getDouble(i);
                groupArray[i] = groupMap.groupAt(i);
            }

            if(groupMap.getGroupCount() == 0) {
                // All rows have at least one missing dimension
                return new MeasureResultSet(dimensionSet, Collections.emptyList());
            }

            // Aggregate into groups
            int numGroups = groupMap.getGroupCount();
            double aggregatedValues[] = Aggregation.aggregate(aggregationFunction(measureModel),
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

    private static StatFunction aggregationFunction(MeasureModel measure) {
        ExprFunction function = ExprFunctions.get(measure.getAggregation());
        if(!(function instanceof StatFunction)) {
            throw new UnsupportedOperationException("aggregation: " + measure.getAggregation());
        }
        return (StatFunction) function;
    }

    private static DimensionMapping findMapping(DimensionModel dimension, MeasureModel measureModel) {
        for (DimensionMapping mapping : dimension.getMappings()) {
            if(mapping.getFormId() == null) {
                return mapping;
            }
        }
        return null;
    }
}
