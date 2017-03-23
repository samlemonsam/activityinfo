package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.ui.client.analysis.model.DimensionModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Immutable set of dimensions
 */
public class DimensionSet implements Iterable<DimensionModel> {

    private int count;
    private DimensionModel[] dimensions;

    public DimensionSet() {
        count = 0;
        dimensions = new DimensionModel[0];
    }

    public DimensionSet(List<DimensionModel> dimensions) {
        List<DimensionModel> activeDimensions = new ArrayList<>();
        for (DimensionModel dimension : dimensions) {
            if(!dimension.getMappings().isEmpty()) {
                activeDimensions.add(dimension);
            }
        }
        this.count = activeDimensions.size();
        this.dimensions = activeDimensions.toArray(new DimensionModel[count]);
    }

    public int getCount() {
        return count;
    }

    public DimensionModel getDimension(int i) {
        return dimensions[i];
    }

    public int getIndex(DimensionModel dimensionModel) {
        for (int i = 0; i < dimensions.length; i++) {
            if(dimensions[i].getId().equals(dimensionModel.getId())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Dimension " + dimensionModel.getId() + " is not a member of this set");
    }

    public List<DimensionModel> getList() {
        return Arrays.asList(dimensions);
    }

    public DimensionSet add(DimensionModel newDimension) {
        List<DimensionModel> newList = new ArrayList<>(getList());
        newList.add(newDimension);
        return new DimensionSet(newList);
    }

    @Override
    public Iterator<DimensionModel> iterator() {
        return Arrays.asList(dimensions).iterator();
    }

}
