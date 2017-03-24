package org.activityinfo.ui.client.analysis.model;

import java.util.List;
import java.util.Optional;

/**
 * A Dimension has a number of discrete categories into which measures are disaggregated.
 *
 * <p>Importantly, a dimension must break up quantitative data from multiple data sources into common
 * categories. </p>
 */
@org.immutables.value.Value.Immutable
public abstract class DimensionModel {

    public static final String STATISTIC_ID = "statistic";

    public abstract String getId();
    public abstract String getLabel();
    public abstract List<DimensionMapping> getMappings();

    @org.immutables.value.Value.Default
    public Axis getAxis() {
        return Axis.ROW;
    }

    @org.immutables.value.Value.Default
    public boolean getTotals() {
        return false;
    }
    public abstract Optional<DateLevel> getDateLevel();

    public abstract Optional<String> getTotalLabel();

    @org.immutables.value.Value.Default
    public boolean getPercentage() {
        return false;
    }

}
