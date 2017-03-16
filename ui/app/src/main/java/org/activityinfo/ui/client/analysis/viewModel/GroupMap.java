package org.activityinfo.ui.client.analysis.viewModel;


import org.activityinfo.model.query.ColumnSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class GroupMap {

    private final int dimCount;
    private final DimensionReader readers[];


    /**
     * Maps a key to it's group id
     */
    private Map<String, Integer> map = new HashMap<>();


    private List<String[]> groups = new ArrayList<>();

    private Function<Integer, String> keyBuilder;

    public GroupMap(DimensionSet dimensions, ColumnSet columnSet, List<DimensionReaderFactory> readerFactories) {
        this.dimCount = dimensions.getCount();
        this.readers = new DimensionReader[dimCount];
        for (int i = 0; i < dimCount; i++) {
            readers[i] = readerFactories.get(i).createReader(columnSet);
        }

        // Try unrolling for small number of dimensions...
        // TODO: actually measure this performance
        if(dimCount == 0) {
            this.keyBuilder = (row -> "");
        } else if(dimCount == 1) {
            this.keyBuilder = this::key1;
        } else if(dimCount == 2) {
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
        StringBuilder key = new StringBuilder(readers[0].read(row));
        for (int i = 1; i < readers.length; i++) {
            key.append('\0').append(readers[i].read(row));
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
        String[] dims = new String[dimCount];
        for (int i = 0; i < dimCount; i++) {
            dims[i]  = readers[i].read(rowIndex);
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
     * Creates a new 'totals' key by omitting a single dimension
     */
    private String rekey(String[] group, boolean[] totalDimension) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < group.length; i++) {
            if(!totalDimension[i]) {
                key.append('\0');
                key.append(group[i]);
            }
        }
        return key.toString();
    }


    private String[] regroup(String[] group, boolean[] totalDimensions) {
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

    /**
     * Merges an existing group array to produce a group array for totals of a single dimension
     */
    public Regrouping total(int[] groupArray, boolean[] totalDimensions) {

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

}
