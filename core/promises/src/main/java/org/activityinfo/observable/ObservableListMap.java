package org.activityinfo.observable;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class ObservableListMap<T, R> extends ObservableList<R> {

    private final ObservableList<T> source;
    private Subscription sourceSubscription = null;

    private final Function<T, R> function;

    private List<R> results = null;
    private Map<T, R> resultMap = null;

    ObservableListMap(ObservableList<T> source, Function<T, R> function) {
        this.source = source;
        this.function = function;
    }

    @Override
    public boolean isLoading() {
        return results == null;
    }


    @Override
    protected void onConnect() {

        sourceSubscription = source.subscribe(new ListObserver<T>() {
            @Override
            public void onChange() {
                rebuildList();
                ObservableListMap.this.fireChanged();
            }

            @Override
            public void onElementAdded(T element) {
                ObservableListMap.this.fireAdded(map(element));
            }

            @Override
            public void onElementRemoved(T element) {
                R resultItem = resultMap.remove(element);
                results.remove(resultItem);
                ObservableListMap.this.fireRemoved(resultItem);
            }
        });
    }

    @Override
    protected void onDisconnect() {
        sourceSubscription.unsubscribe();
    }

    private void rebuildList() {
        if (source.isLoading()) {
            results = null;
            resultMap = null;
        } else {
            results = new ArrayList<>();
            resultMap = new HashMap<>();
            for (T sourceItem : source.getList()) {
                map(sourceItem);
            }
        }
    }

    private R map(T sourceItem) {
        R resultItem = function.apply(sourceItem);
        assert resultItem != null : "result of list map operation was null";
        results.add(resultItem);
        resultMap.put(sourceItem, resultItem);
        return resultItem;
    }


    @Override
    public List<R> getList() {
        assert results != null : "The list is not loaded.";
        List<R> resultList = new ArrayList<>();
        for (R result : results) {
            resultList.add(result);
        }
        return resultList;
    }
}
