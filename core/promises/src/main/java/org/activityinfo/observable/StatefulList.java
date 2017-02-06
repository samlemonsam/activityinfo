package org.activityinfo.observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates model state represented as a set
 */
public class StatefulList<T> extends ObservableList<T> {

    private List<T> list = new ArrayList<>();

    @Override
    public boolean isLoading() {
        return false;
    }

    public void add(T item) {
        list.add(item);
        fireAdded(item);
    }

    public void set(T keepItem) {
        list.clear();
        list.add(keepItem);

        fireChanged();
    }

    public void clear() {
        list.clear();
        fireChanged();
    }

    @Override
    public List<T> getList() {
        return Collections.unmodifiableList(list);
    }
}
