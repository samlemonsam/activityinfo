/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
            activeDimensions.add(dimension);
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

    /**
     *
     * @return the index of the Dimension with the given {@code dimensionId},
     * or -1 if no such dimension is included.
     */
    public int getIndexByDimensionId(String dimensionId) {
        for (int i = 0; i < dimensions.length; i++) {
            if (dimensions[i].getId().equals(dimensionId)) {
                 return i;
            }
        }
        return -1;
    }
}
