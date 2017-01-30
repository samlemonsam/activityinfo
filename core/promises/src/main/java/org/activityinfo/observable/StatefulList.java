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

    @Override
    public List<T> asList() {
        return Collections.unmodifiableList(list);
    }
}
