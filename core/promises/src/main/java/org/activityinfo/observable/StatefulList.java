package org.activityinfo.observable;

import com.google.common.base.Predicate;

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

    /**
     * Removes the first element that matches the given {@code predicate}.
     *
     * @return true if a matching element was found and removed.
     */
    public boolean removeFirst(Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if(predicate.apply(list.get(i))) {
                T removed = list.remove(i);
                fireRemoved(removed);
                return true;
            }
        }
        return false;
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
