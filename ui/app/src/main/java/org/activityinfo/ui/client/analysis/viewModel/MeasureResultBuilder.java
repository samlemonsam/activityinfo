package org.activityinfo.ui.client.analysis.viewModel;


import com.google.common.annotations.VisibleForTesting;
import org.activityinfo.model.expr.functions.*;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.Aggregation;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.Statistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;


public class MeasureResultBuilder {


    private final EffectiveMeasure measure;

    private final DimensionSet dimensionSet;

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

    private final int measureDimensionIndex;


    /**
     * The index of the "statistic" dimension, or -1 if not requested.
     */
    private int statisticDimensionIndex;

    /**
     * Dimensions which require totals
     */
    private final boolean[] totalsRequired;

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
        this.numDims = measure.getDimensionSet().getCount();
        this.measureDimensionIndex = measure.getDimensionSet().getIndexByDimensionId(DimensionModel.MEASURE_ID);
        this.statisticDimensionIndex = measure.getDimensionSet().getIndexByDimensionId(DimensionModel.STATISTIC_ID);
        this.totalsRequired = whichDimensionsRequireTotals();

    }

    private boolean[] whichDimensionsRequireTotals() {
        boolean[] totals = new boolean[dimensionSet.getCount()];
        for (int i = 0; i < dimensionSet.getCount(); i++) {
            DimensionModel dim = dimensionSet.getDimension(i);
            if(dim.getTotals() || dim.getPercentage()) {
                totals[i] = true;
            }
        }
        return totals;
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
            double[] groupedValues = aggregate(statistic, valueArray, groupArray, groupMap.getGroups());

            /*
             * In addition to the values for the full dimension set, totals may be requested for one or
             * more dimensions. This essentially means re-computing totals for different subsets of the dimensions.
             *
             * The subset array indicates which dimensions should excluded in this round and thus totaled.
             */
            boolean subset[] = new boolean[numDims];
            while (nextSubset(subset, totalsRequired)) {
                Regrouping regrouping = groupMap.total(groupArray, subset);

                if(statistic == Statistic.SUM) {
                    totalSums(regrouping, groupedValues);
                } else {
                    aggregate(statistic, valueArray, regrouping.getNewGroupArray(), regrouping.getNewGroups());
                }
            }
        }
    }

    private void totalSums(Regrouping regrouping, double[] groupSums) {
        boolean includeTotals = includeTotals(regrouping);
        boolean includePercentages = includePercentages(regrouping);

        double[] totals = new double[regrouping.getNewGroupCount()];
        int[] map = regrouping.getMap();

        // First, merge the individual group sums into
        // totals by the selected dimensions
        for (int oldGroup = 0; oldGroup < groupSums.length; oldGroup++) {
            int newGroup = map[oldGroup];
            totals[newGroup] += groupSums[oldGroup];
        }

        // Add a point for each of the collapsed groups
        if(includeTotals) {
            List<String[]> newGroups = regrouping.getNewGroups();
            for (int i = 0; i < totals.length; i++) {
                points.add(new Point(totals[i],
                        format(totals[i]),
                        withFixedDimensions(newGroups.get(i), Statistic.SUM)));
            }
        }

        // Now add percentages if they are requested
        String percentageLabel = composePercentageLabel(regrouping);
        if(includePercentages) {
            for (int oldGroup = 0; oldGroup < groupSums.length; oldGroup++) {
                int newGroup = map[oldGroup];
                double conditionalProbability = groupSums[oldGroup] / totals[newGroup];

                points.add(new Point(conditionalProbability,
                        formatPercentage(conditionalProbability),
                        withFixedDimensions(regrouping.getOldGroup(oldGroup), percentageLabel)));
            }
        }

        if(includeTotals && includePercentages) {
            for (String[] newGroup : regrouping.getNewGroups()) {
                points.add(new Point(1.0, formatPercentage(1.0), withFixedDimensions(newGroup, percentageLabel)));
            }
        }
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

    private boolean includePercentages(Regrouping regrouping) {
        DimensionSet dimensionSet = measure.getDimensionSet();
        for (int i = 0; i < dimensionSet.getCount(); i++) {
            if (regrouping.isDimensionTotaled(i)) {
                if(!dimensionSet.getDimension(i).getPercentage()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean includeTotals(Regrouping regrouping) {
        DimensionSet dimensionSet = measure.getDimensionSet();
        for (int i = 0; i < dimensionSet.getCount(); i++) {
            if (regrouping.isDimensionTotaled(i)) {
                if(!dimensionSet.getDimension(i).getTotals()) {
                    return false;
                }
            }
        }
        return true;
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

    private boolean isCommutative(Statistic statistic) {
        switch (statistic) {
            case COUNT:
            case SUM:
            case MIN:
            case MAX:
                return true;
            default:
                return false;
        }
    }


    /**
     * Finds the next combination of dimensions to include in the aggregation.
     */
    @VisibleForTesting
    static boolean nextSubset(boolean[] subset, boolean[] totalsRequired) {
        // Find the right-most dimension we can "increment"
        int i = subset.length - 1;
        while(i >= 0) {
            if(!subset[i] && totalsRequired[i]) {
                subset[i] = true;
                Arrays.fill(subset, i+1, subset.length, false);
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
                     double value = valueArray[i];
                     if(!Double.isNaN(value)) {
                         totals[groupIndex] += value;
                     }
                 }
            }

            for (int i = 0; i < totals.length; i++) {
                String[] group =  category.group(groupMap.getGroup(i));

                points.add(new Point(totals[i], format(totals[i]), withFixedDimensions(group, Statistic.SUM)));
            }
        }
    }

    private double[] aggregate(Statistic statistic, double[] valueArray, int[] groupId, List<String[]> groups) {

        StatFunction stat = aggregationFunction(statistic);

        double aggregatedValues[] = Aggregation.aggregate(
                stat,
                groupId,
                valueArray,
                valueArray.length,
                groups.size());

        for (int i = 0; i < groups.size(); i++) {
            points.add(new Point(aggregatedValues[i], format(aggregatedValues[i]), withFixedDimensions(groups.get(i), statistic)));
        }

        return aggregatedValues;
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
        return new MeasureResultSet(measure.getDimensionSet(), points);
    }
}
