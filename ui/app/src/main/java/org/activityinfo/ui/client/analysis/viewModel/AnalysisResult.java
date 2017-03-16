package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.annotations.VisibleForTesting;
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
import java.util.Arrays;
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

            List<Point> points = new ArrayList<Point>();

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

            StatFunction stat = aggregationFunction(measureModel);

            // Aggregate into groups
           aggregate(points, stat, valueArray, groupArray, groupMap.getGroups());

            // Add total points for those dimensions that require totals
            boolean total[] = new boolean[dimensionSet.getCount()];
            while (nextTotalSet(dimensionSet, total)) {
                Regrouping regrouping = groupMap.total(groupArray, total);
                aggregate(points, stat, valueArray, regrouping.getGroupArray(), regrouping.getGroups());
            }

            return new MeasureResultSet(dimensionSet, points);
        });

        return resultSet;
    }

    @VisibleForTesting
    static boolean nextTotalSet(DimensionSet dimensionSet, boolean[] total) {
        // Find the right-most dimension we can "increment"
        int i = total.length - 1;
        while(i >= 0) {
            if(!total[i] && dimensionSet.getDimension(i).isTotalIncluded()) {
                total[i] = true;
                Arrays.fill(total, i+1, total.length, false);
                return true;
            }
            i--;
        }
        return false;
    }

    private static void aggregate(List<Point> points, StatFunction stat, double[] valueArray, int[] groupArray, List<String[]> groups) {

        double aggregatedValues[] = Aggregation.aggregate(stat,
                groupArray,
                valueArray,
                valueArray.length,
                groups.size());

        for (int i = 0; i < groups.size(); i++) {
            points.add(new Point(groups.get(i), aggregatedValues[i]));
        }
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
