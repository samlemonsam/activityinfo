package org.activityinfo.ui.client.analysis.viewModel;


import org.activityinfo.model.query.ColumnSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class GroupMap {

    /**
     * Number of actually used dimensions. May be fewer than the total number of dimensions
     * in the analysis
     */
    private int keyDim;

    private final List<EffectiveMapping> dimensions;

    private final DimensionReader readers[];
    private final int keyDimIndexes[];


    /**
     * Maps a key to it's group id
     */
    private Map<String, Integer> map = new HashMap<>();


    private List<String[]> groups = new ArrayList<>();

    private Function<Integer, String> keyBuilder;

    public GroupMap(ColumnSet columnSet, List<EffectiveMapping> dims) {
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
    public int groupAt(int rowIndex) {
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

    public int getGroupCount() {
        return groups.size();
    }

    public String[] getGroup(int groupId) {
        return groups.get(groupId);
    }

    public List<String[]> getGroups() {
        return groups;
    }


    /**
     * Merges an existing group array to produce a group array for totals of a single dimension
     */
    public Regrouping total(int[] groupArray, boolean[] totalDimensions) {

        if(!any(totalDimensions)) {
            return new Regrouping(groupArray, groups);
        }

        int map[] = new int[getGroupCount()];

        List<String[]> newGroups = new ArrayList<>();
        Map<String, Integer> newGroupMap = new HashMap<>();

        // Merge the existing groups into superset by omitting the
        // total dimension
        for (int i = 0; i < groups.size(); i++) {
            String[] group = groups.get(i);
            String regroupedKey = rekey(group, totalDimensions);

            Integer regroupedIndex = newGroupMap.get(regroupedKey);
            if(regroupedIndex == null) {
                String regroup[] = regroup(group, totalDimensions);

                regroupedIndex = newGroups.size();
                newGroups.add(regroup);
                newGroupMap.put(regroupedKey, regroupedIndex);
            }
            map[i] = regroupedIndex;
        }

        // Now map the group array to the new indexes
        int[] regroupedArray = new int[groupArray.length];
        for (int i = 0; i < groupArray.length; i++) {
            int oldGroup = groupArray[i];
            if(oldGroup == -1) {
                regroupedArray[i] = -1;
            } else {
                regroupedArray[i] = map[oldGroup];
            }
        }

        return new Regrouping(regroupedArray, newGroups);
    }

    private boolean any(boolean[] x) {
        for (int i = 0; i < x.length; i++) {
            if(x[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new 'totals' key by omitting totalled dimensions
     *
     * @param group an array of dimension categories
     * @param totalDimensions an array indicating which dimensions should be totaled and so omitted from the key.
     */

    private String rekey(String[] group, boolean[] totalDimensions) {
        assert group.length == dimensions.size();
        assert group.length == totalDimensions.length;

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < group.length; i++) {
            if(!totalDimensions[i]) {
                key.append('\0');
                key.append(group[i]);
            }
        }
        return key.toString();
    }

    private String[] regroup(String[] group, boolean[] totalDimensions) {
        assert group.length == dimensions.size();

        String[] regrouped = new String[group.length];
        for (int i = 0; i < group.length; i++) {
            if(totalDimensions[i]) {
                regrouped[i] = "Total";
            } else {
                regrouped[i] = group[i];
            }
        }
        return regrouped;
    }
}
