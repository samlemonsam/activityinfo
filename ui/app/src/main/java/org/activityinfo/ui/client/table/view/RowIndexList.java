package org.activityinfo.ui.client.table.view;

import java.util.AbstractList;

public class RowIndexList extends AbstractList<Integer> {

    private int offset;
    private int length;

    public RowIndexList(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    @Override
    public Integer get(int index) {
        return offset + index;
    }

    @Override
    public int size() {
        return length;
    }
}
