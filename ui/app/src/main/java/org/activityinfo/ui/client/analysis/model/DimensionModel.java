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

    public abstract String id();
    public abstract String label();
    public abstract List<DimensionMapping> mappings();

    @org.immutables.value.Value.Default
    public boolean totalIncluded() {
        return false;
    }
    public abstract Optional<DateLevel> dateLevel();

}
