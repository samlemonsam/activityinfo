package org.activityinfo.ui.client.analysis.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A Dimension has a number of discrete categories into which measures are disaggregated.
 *
 * <p>Importantly, a dimension must break up quantitative data from multiple data sources into common
 * categories. </p>
 */
public class DimensionModel {

    private String id;
    private String label;
    private List<DimensionMapping> mappings;
    private boolean totalIncluded;

    public DimensionModel(String id, String label, List<DimensionMapping> mappings) {
        this.id = id;
        this.label = label;
        this.mappings = ImmutableList.copyOf(mappings);
    }

    public DimensionModel(String id, String label, DimensionMapping... mappings) {
        this.id = id;
        this.label = label;
        this.mappings = ImmutableList.copyOf(mappings);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public List<DimensionMapping> getMappings() {
        return mappings;
    }


    /**
     * @return true if a total column is included.
     */
    public boolean isTotalIncluded() {
        return totalIncluded;
    }

    public DimensionModel setTotalIncluded(boolean totalIncluded) {
        this.totalIncluded = totalIncluded;
        return this;
    }
}
