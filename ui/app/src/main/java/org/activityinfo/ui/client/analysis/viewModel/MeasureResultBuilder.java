package org.activityinfo.ui.client.analysis.viewModel;


import com.google.common.annotations.VisibleForTesting;
import org.activityinfo.model.expr.functions.*;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.Aggregation;
import org.activityinfo.ui.client.analysis.model.Statistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;


public class MeasureResultBuilder {


    private final EffectiveMeasure measure;

    private ColumnSet columns;

    private final int numRows;

    /**
     * Total number of dimensions
     */
    private final int numDims;


    /**
     * List of all points in the multi-dimensional space resulting from this measure.
     */
    private final List<Point> points = new ArrayList<>();
    private MultiDimSet multiDimSet;


    /**
     *
     * @param measure the effective model of this measure, fully resolved.
     * @param columns the results of the base query.
     */
    public MeasureResultBuilder(EffectiveMeasure measure, ColumnSet columns) {
        this.measure = measure;
        this.columns = columns;
        this.numRows = columns.getNumRows();
        this.numDims = measure.getDimensionSet().getCount();

    }

    public void execute() {

        /*
         * The value column contains the numerical values that we will be aggregating
         * by dimension.
         */
        ColumnView value = columns.getColumnView("value");


        /*
         * The first step is to divide the value vector into groups defined by the intersection
         * of all single-valued dimensions.
         *
         * For example, if this analysis includes the "Gender" and "Married" dimensions, which
         * take the categories "Female" and "Male" and "Married" and "Single" respectively, then
         * the GroupMap helps us match the combinations to a group index. For example:
         *
         * Female-Married -> Group 0
         * Male-Married ->   Group 1
         * Female-Single ->  Group 2
         * Male-Single ->    Group 3
         *
         */
        GroupMap groupMap = new GroupMap(columns, measure.getDimensions());


        /*
         * Using the GroupMap, we want to build parallel arrays containing the measure's numerical
         * values in one array, and the group index in the other. Continuing the example above,
         * the result might be:
         *
         * valueArray = [1.5, 34, 10, 1.333, 22, 14]
         * groupArray = [  1,  0,  3,     0,  2, -1]
         *
         * Where -1 means that either Gender or Married was missing for this value, and so the value
         * is discarded.
         */
        double valueArray[] = new double[numRows];
        int groupArray[] = new int[numRows];

        for (int i = 0; i < numRows; i++) {
            valueArray[i] = value.getDouble(i);
            groupArray[i] = groupMap.groupAt(i);
        }

        /*
         * If no groups were found, that means that all measure values have at least one missing dimension
         * and so all values are discarded.
         */
        if(groupMap.getGroupCount() == 0) {
            return;
        }

        /*
         * Dimensions that can have multiple values further complicate matters. We need to run the aggregation
         * multiple times as a single value can be counted towards multiple categories.
         */
        multiDimSet = buildMultiDimSet();

        if(multiDimSet.isEmpty()) {

            /*
             * Without multi-valued dimensions, the best strategy is probably sort+aggregate
             */
            sortAndAggregate(valueArray, groupArray, groupMap);

        } else {

            aggregateMulti(valueArray, groupArray, groupMap);
        }

    }


    private MultiDimSet buildMultiDimSet() {
        List<MultiDim> dimSets = new ArrayList<>();
        for (EffectiveMapping dimension : measure.getDimensions()) {
            if(dimension.isMultiValued()) {
                dimSets.add(dimension.createMultiDimSet(columns));
            }
        }
        return new MultiDimSet(dimSets);
    }


    private void sortAndAggregate(double[] valueArray, int[] groupArray, GroupMap groupMap) {

        for (Statistic statistic : measure.getModel().getStatistics()) {


            /*
             * We always do an initial aggregation that includes all the dimensions
             */
            aggregate(statistic, valueArray, groupArray, groupMap.getGroups());

            /*
             * In addition to the values for the full dimension set, totals may be requested for one or
             * more dimensions. This essentially means re-computing totals for different subsets of the dimensions.
             *
             * The subset array indicates which dimensions should excluded in this round and thus totaled.
             */
            boolean subset[] = new boolean[numDims];
            while (nextSubset(measure.getDimensionSet(), subset)) {
                Regrouping regrouping = groupMap.total(groupArray, subset);
                aggregate(statistic, valueArray, regrouping.getGroupArray(), regrouping.getGroups());
            }
        }

    }


    /**
     * Finds the next combination of dimensions to include in the aggregation.
     */
    @VisibleForTesting
    static boolean nextSubset(DimensionSet dimensionSet, boolean[] excludedDimensions) {
        // Find the right-most dimension we can "increment"
        int i = excludedDimensions.length - 1;
        while(i >= 0) {
            if(!excludedDimensions[i] && dimensionSet.getDimension(i).getTotals()) {
                excludedDimensions[i] = true;
                Arrays.fill(excludedDimensions, i+1, excludedDimensions.length, false);
                return true;
            }
            i--;
        }
        return false;
    }


    private void aggregateMulti(double[] valueArray, int[] groupArray, GroupMap groupMap) {

        /* For each category combination ... */

        List<MultiDimCategory> categories = multiDimSet.build();

        for (MultiDimCategory category : categories) {

            double totals[] = new double[groupMap.getGroupCount()];

            BitSet bitSet = category.getBitSet();

            for (int i = 0; i < valueArray.length; i++) {
                 if(bitSet.get(i)) {
                     int groupIndex = groupArray[i];
                     totals[groupIndex] += valueArray[i];
                 }
            }

            for (int i = 0; i < totals.length; i++) {
                String[] group =  category.group(groupMap.getGroup(i));

                points.add(new Point(totals[i], group));
            }
        }
    }

    private void aggregate(Statistic statistic, double[] valueArray, int[] groupId, List<String[]> groups) {

        StatFunction stat = aggregationFunction(statistic);

        double aggregatedValues[] = Aggregation.aggregate(
                stat,
                groupId,
                valueArray,
                valueArray.length,
                groups.size());

        for (int i = 0; i < groups.size(); i++) {
            points.add(new Point(aggregatedValues[i], statistic, groups.get(i)));
        }
    }

    private static StatFunction aggregationFunction(Statistic statistic) {
        switch (statistic) {
            case COUNT:
                return CountFunction.INSTANCE;
            case SUM:
                return SumFunction.INSTANCE;
            case AVERAGE:
                return AverageFunction.INSTANCE;
            case MEDIAN:
                return MedianFunction.INSTANCE;
            case MIN:
                return MinFunction.INSTANCE;
            case MAX:
                return MaxFunction.INSTANCE;

            default:
                throw new IllegalArgumentException("aggregation: " + statistic);
        }
    }


    public MeasureResultSet getResult() {
        return new MeasureResultSet(measure.getDimensionSet(), points);
    }
}
