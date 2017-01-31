package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreClearEvent;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

/**
 * An adapter for ActivityInfo's {@link ColumnSet} objects, which are column-oriented rather than
 * row oriented.
 */
public class ColumnSetStore extends ListStore<Integer> {


    private enum RowIndexKeyProvider implements ModelKeyProvider<Integer> {
        INSTANCE;

        @Override
        public String getKey(Integer item) {
            return item.toString();
        }
    }

    private Observable<ColumnSet> columnSet;
    private Subscription subscription;

    /**
     * Creates a new store.
     *
     * @param columnSet
     */
    public ColumnSetStore(Observable<ColumnSet> columnSet) {
        super(RowIndexKeyProvider.INSTANCE);
        this.columnSet = columnSet;
        this.subscription = this.columnSet.subscribe(observable -> {
            if (observable.isLoading()) {
                fireEvent(new StoreClearEvent<Integer>());
            } else {
                fireEvent(new StoreAddEvent<>(0, getAll()));
            }
        });
    }

    @Override
    public void add(int index, Integer item) {
        // Rows cannot be added to the store
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Integer item) {
        // Rows cannot be added to the store
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Integer> items) {
        // Rows cannot be added to the store
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Integer> items) {
        // Rows cannot be added to the store
        throw new UnsupportedOperationException();
    }

    @Override
    public void applySort(boolean suppressEvent) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void clear() {
        // Rows cannot be removed from the store
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer findModelWithKey(String key) {
        // The key is just the row index as a string. Just convert it back.
        return Integer.parseInt(key);
    }

    @Override
    public Integer get(int index) {
        return index;
    }

    @Override
    public List<Integer> getAll() {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return i;
            }

            @Override
            public int size() {
                return ColumnSetStore.this.size();
            }
        };
    }

    @Override
    public int indexOf(Integer item) {
        return item;
    }

    @Override
    public Integer remove(int index) {
        // Rows cannot be removed from the store
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Integer model) {
        // Rows cannot be removed from the store
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(List<? extends Integer> newItems) {
        // Rows cannot be changed
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        if(columnSet.isLoading()) {
            return 0;
        } else {
            return columnSet.get().getNumRows();
        }
    }

    @Override
    public List<Integer> subList(final int start, final int end) {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return start + i;
            }

            @Override
            public int size() {
                return end - start;
            }
        };
    }

    @Override
    public void update(Integer rowIndex) {
        // Rows cannot be changed
        throw new UnsupportedOperationException();
    }

    @Override
    protected void applyFilters() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    protected boolean isFilteredOut(Integer item) {
        return false;
    }

    public ValueProvider<Integer, String> stringValueProvider(final String columnId) {
        return new ValueProvider<Integer, String>() {
            @Override
            public String getValue(Integer rowIndex) {
                return columnSet.get().getColumnView(columnId).getString(rowIndex);
            }

            @Override
            public void setValue(Integer object, String value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPath() {
                return columnId;
            }
        };
    }
}
