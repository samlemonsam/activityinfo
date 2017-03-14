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
        } else if(dimCount == 3) {
            this.keyBuilder = this::key3;
        } else {
            this.keyBuilder = this::key;
        }
    }

    private String key1(int rowIndex) {
        return readers[0].read(rowIndex);
    }

    private String key2(int rowIndex) {
        return readers[0].read(rowIndex) + "\0" + readers[1].read(rowIndex);
    }

    private String key3(int rowIndex) {
        return readers[0].read(rowIndex) + "\0" +
               readers[1].read(rowIndex) + "\0" +
               readers[2];
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
        return Math.max(1, groups.size());
    }

    public String[] getGroup(int groupId) {
        if(groups.isEmpty() && groupId == 0) {
            return new String[dimCount];
        }
        return groups.get(groupId);
    }
}
