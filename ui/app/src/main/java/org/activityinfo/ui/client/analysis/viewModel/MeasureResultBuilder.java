package org.activityinfo.ui.client.analysis.viewModel;


import org.activityinfo.model.expr.functions.*;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.Aggregation;
import org.activityinfo.store.query.shared.HeapsortTandem;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.Statistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MeasureResultBuilder {


    private final EffectiveMeasure measure;

    private final DimensionSet dimensionSet;

    private ColumnSet columns;

    private final int numRows;

    /**
     * List of all points in the multi-dimensional space resulting from this measure.
     */
    private final List<Point> points = new ArrayList<>();

    private final List<Point> percentages = new ArrayList<>();

    private final int measureDimensionIndex;


    /**
     * The index of the "statistic" dimension, or -1 if not requested.
     */
    private final int statisticDimensionIndex;

    /**
     *
     * @param measure the effective model of this measure, fully resolved.
     * @param columns the results of the base query.
     */
    public MeasureResultBuilder(EffectiveMeasure measure, ColumnSet columns) {
        this.measure = measure;
        this.dimensionSet = measure.getDimensionSet();
        this.columns = columns;
        this.numRows = columns.getNumRows();
        this.measureDimensionIndex = measure.getDimensionSet().getIndexByDimensionId(DimensionModel.MEASURE_ID);
        this.statisticDimensionIndex = measure.getDimensionSet().getIndexByDimensionId(DimensionModel.STATISTIC_ID);

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

        /*
         * Using the GroupMapBuilder, we want to build parallel arrays containing the measure's numerical
         * values in one array, and the group index in the other. Continuing the example above,
         * the result might be:
         *
         * valueArray = [1.5, 34, 10, 1.333, 22, 14]
         * groupArray = [  1,  0,  3,     0,  2, -1]
         *
         * Where -1 means that either Gender or Married was missing for this value, and so the value
         * is discarded.
         */

        GroupMap groupMap = GroupMapBuilder.build(columns, measure.getDimensions());

        /*
         * If no groups were found, that means that all measure values have at least one missing dimension
         * and so all values are discarded.
         */
        if(groupMap.getGroupCount() == 0) {
            return;
        }

        double valueArray[] = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            valueArray[i] = value.getDouble(i);
        }


        MultiDimSet multiDimSet = buildMultiDimSet();


        /*
         * If totals are included for any of the dimensions, we need to repeat the aggregation
         * several times, omitting different dimensions at different iterations.
         */
        for(TotalSubset subset : TotalSubset.set(dimensionSet)) {

            /*
             * Regroup single-valued dimensions
             */
            GroupMap singleValuedDims = groupMap.regroup(subset);

            /*
             * Regroup multi-valued dimensions
             */
            MultiDimSet multiValuedDims = multiDimSet.regroup(subset);

            /*
             * Do the aggregation!
             */
            aggregate(subset, singleValuedDims, multiValuedDims, valueArray);
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


    private void aggregate(
        TotalSubset totalSubset,
        GroupMap singleValuedDims,
        MultiDimSet multiValuedDims,
        double[] valueArray) {


        /*
         * For dimensions that can have multiple values, we need to run the aggregation
         * multiple times as a single value can be counted towards multiple categories.
         */
        for (MultiDimCategory multiDimCategory : multiValuedDims.build()) {

            int[] groupArray = singleValuedDims.copyOfGroupArray();

            /*
             * Exclude the values that do not belong to this multiDimCategory
             * by marking them as NaN.
             */
            double[] filteredValues = multiDimCategory.filter(valueArray);

            /*
             * Sort the group and value arrays in tandem
             * (We can only do this in the inner loop because we need the original
             *  order for applying the multi-dimensional category filter)
             */

            HeapsortTandem.heapsortDescending(groupArray, filteredValues, filteredValues.length);

            /*
             * Now calculated all the required statistics.
             */
            for (Statistic statistic : measure.getModel().getStatistics()) {
                aggregate(totalSubset, singleValuedDims, multiDimCategory, statistic, groupArray, filteredValues);
            }
        }
    }

    private double[] aggregate(TotalSubset totalSubset,
                               GroupMap singleValuedDims,
                               MultiDimCategory multiDimCategory,
                               Statistic statistic,
                               int[] sortedGroupArray,
                               double[] sortedValueArray) {

        StatFunction stat = aggregationFunction(statistic);

        int numSingleValuedGroups = singleValuedDims.getGroupCount();

        double aggregatedValues[] = Aggregation.aggregateSorted(
            stat,
            sortedGroupArray,
            sortedValueArray,
            sortedValueArray.length,
            numSingleValuedGroups);

        boolean includeTotals = totalSubset.includeTotals();
        boolean includePercentages = statistic == Statistic.SUM && totalSubset.includePercentages();

        for (int i = 0; i < numSingleValuedGroups; i++) {
            String[] group = multiDimCategory.withMultiValuedCategories(singleValuedDims.getGroup(i));

            if(includeTotals) {
                points.add(new Point(
                    aggregatedValues[i],
                    format(aggregatedValues[i]),
                    withFixedDimensions(group, statistic)));
            }
            if( includePercentages) {
                addPercentage(group, aggregatedValues[i]);
            }
        }
        return aggregatedValues;
    }

    private void addPercentage(String[] group, double denominator) {

        for (Point point : points) {
            if(isNumerator(point, group)) {
                double percentage = point.getValue() / denominator;

                percentages.add(new Point(percentage, formatPercentage(percentage),
                    withPercentage(point, "%")));
            }
        }
    }

    private String[] withPercentage(Point point, String percentageLabel) {
        String[] group = new String[dimensionSet.getCount()];
        for (int i = 0; i < group.length; i++) {
            group[i] = point.getCategory(i);
        }
        group[statisticDimensionIndex] = percentageLabel;
        return group;
    }

    private boolean isNumerator(Point point, String[] group) {
        for (int i = 0; i < group.length; i++) {
            if(i != statisticDimensionIndex) {
                String numeratorCategory = point.getCategory(i);
                String denominatorCategory = group[i];
                if(!denominatorCategory.equals(Point.TOTAL) &&
                    !numeratorCategory.equals(denominatorCategory)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String composePercentageLabel(Regrouping regrouping) {
        StringBuilder s = new StringBuilder("%");

        if(!isGrandTotal(regrouping)) {
            for (int i = 0; i < dimensionSet.getCount(); i++) {
                if(regrouping.isDimensionTotaled(i)) {
                    s.append(" ");
                    s.append(dimensionSet.getDimension(i).getLabel());
                }
            }
        }
        return s.toString();
    }

    public boolean isGrandTotal(Regrouping regrouping) {
        for (EffectiveMapping dim : measure.getDimensions()) {
            if(!dim.getId().equals(DimensionModel.STATISTIC_ID) && !dim.getId().equals(DimensionModel.MEASURE_ID)) {
                if(!regrouping.isDimensionTotaled(dim.getIndex())) {
                    return false;
                }
            }
        }
        return true;
    }

    private String formatPercentage(double probability) {
        return Integer.toString((int)Math.round(probability * 100d)) + "%";
    }

    private String[] withFixedDimensions(String[] group, Statistic statistic) {
        return withFixedDimensions(group, statistic.getLabel());
    }

    private String[] withFixedDimensions(String[] group, String statistic) {
        if(statisticDimensionIndex == -1 && measureDimensionIndex == -1) {
            return group;
        } else {
            String[] groupWithFixed = Arrays.copyOf(group, group.length);
            if(measureDimensionIndex != -1) {
                groupWithFixed[measureDimensionIndex] = measure.getModel().getLabel();
            }
            if(statisticDimensionIndex != -1) {
                groupWithFixed[statisticDimensionIndex] = statistic;
            }
            return groupWithFixed;
        }
    }

    private String format(double value) {
        return Integer.toString((int)Math.round(value));
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
        List<Point> result = new ArrayList<>();
        result.addAll(points);
        result.addAll(percentages);

        return new MeasureResultSet(measure.getDimensionSet(), result);
    }
}
