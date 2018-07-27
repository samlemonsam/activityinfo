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
package org.activityinfo.analysis.pivot.viewModel;

import org.activityinfo.model.query.ColumnSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GroupMapBuilder {

    /**
     * Maps a key to it's group id
     */
    private Map<String, Integer> map = new HashMap<>();

    private List<String[]> groups = new ArrayList<>();

    /**
     * Number of actually used dimensions. May be fewer than the total number of dimensions
     * in the analysis
     */
    private int keyDim;

    private ColumnSet columnSet;
    private final List<EffectiveMapping> dimensions;

    private final DimensionReader readers[];
    private final int keyDimIndexes[];

    private Function<Integer, String> keyBuilder;

    public GroupMapBuilder(ColumnSet columnSet, List<EffectiveMapping> dims) {
        this.columnSet = columnSet;
        this.dimensions = dims;
        this.readers = new DimensionReader[dims.size()];
        this.keyDimIndexes = new int[dims.size()];

        for (EffectiveMapping dim : dims) {
            if(dim.isSingleValued()) {
                DimensionReader reader = dim.createReader(columnSet);
                if (reader != null) {
                    keyDimIndexes[keyDim] = dim.getIndex();
                    readers[keyDim] = reader;
                    keyDim++;
                }
            }
        }

        // Try unrolling for small number of dimensions...
        // TODO: actually measure this performance
        if(keyDim == 0) {
            this.keyBuilder = (row -> "");
        } else if(keyDim == 1) {
            this.keyBuilder = this::key1;
        } else if(keyDim == 2) {
            this.keyBuilder = this::key2;
        } else {
            this.keyBuilder = this::key;
        }
    }

    private String key1(int rowIndex) {
        return readers[0].read(rowIndex);
    }

    private String key2(int rowIndex) {
        String category1 = readers[0].read(rowIndex);
        if(category1 == null) {
            return null;
        }
        String category2 = readers[1].read(rowIndex);
        if(category2 == null) {
            return null;
        }
        return category1 + "\0" + category2;
    }

    private String key(int row) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < this.keyDim; i++) {
            String category = readers[i].read(row);
            if(category == null) {
                return null;
            }
            if(i > 0) {
                key.append('\0');
            }
            key.append(category);
        }
        return key.toString();
    }


    /**
     * Returns the group index at the given row.
     * @param rowIndex the row index
     * @return integer group id
     */
    private int groupAt(int rowIndex) {
        String key = keyBuilder.apply(rowIndex);
        if(key == null) {
            return -1;
        }
        Integer id = map.get(key);
        if(id == null) {
            String[] group = buildGroup(rowIndex);
            id = groups.size();
            groups.add(group);
            map.put(key, id);
        }
        return id;
    }

    private String[] buildGroup(int rowIndex) {
        String[] dims = new String[dimensions.size()];
        for (int i = 0; i < keyDim; i++) {
            int dimIndex = keyDimIndexes[i];
            String dimCategory = readers[i].read(rowIndex);
            dims[dimIndex] = dimCategory;
        }
        return dims;
    }

    public static GroupMap build(ColumnSet columnSet, List<EffectiveMapping> dims) {

        GroupMapBuilder builder = new GroupMapBuilder(columnSet, dims);

        int numRows = columnSet.getNumRows();
        int[] groupArray = new int[numRows];

        for (int i = 0; i < numRows; i++) {
            groupArray[i] = builder.groupAt(i);
        }

        return new GroupMap(dims, builder.groups, groupArray);
    }
}
