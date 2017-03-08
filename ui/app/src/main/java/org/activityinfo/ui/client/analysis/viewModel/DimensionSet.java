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
        this.count = dimensions.size();
        this.dimensions = new DimensionModel[count];
        for (int i = 0; i < count; i++) {
            this.dimensions[i] = dimensions.get(i);
        }
    }

    public int getCount() {
        return count;
    }

    public DimensionModel getDimension(int i) {
        return dimensions[i];
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
