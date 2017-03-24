package org.activityinfo.ui.client.analysis.model;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ImmutableLists {

    public static <T> List<T> update(List<T> list, T updatedItem, Function<T, String> keyProvider) {

        String updatedId = keyProvider.apply(updatedItem);

        List<T> newList = new ArrayList<>(list.size());
        boolean updated = false;
        for (T item : list) {
            String itemId = keyProvider.apply(item);
            if(itemId.equals(updatedId)) {
                newList.add(updatedItem);
                updated = true;
            } else {
                newList.add(item);
            }
        }

        if (!updated) {
            newList.add(updatedItem);
        }
        return newList;
    }

    public static <T> List<T> remove(List<T> list, String id, Function<T, String> keyProvider) {

        List<T> newList = new ArrayList<>(list.size());
        for (T item : list) {
            String itemId = keyProvider.apply(item);
            if(!itemId.equals(id)) {
                newList.add(item);
            }
        }
        return newList;
    }

    public static <T> List<T> reorder(List<T> list, String afterId, List<T> items, Function<T, String> keyProvider) {

        Set<String> toReorder = new HashSet<>();
        for (T item : items) {
            if(!toReorder.add(keyProvider.apply(item))) {
                throw new IllegalArgumentException("Duplicate ids in items");
            }
        }
        boolean added = false;
        List<T> newList = new ArrayList<>(list.size());
        for (T item : list) {
            String itemId = keyProvider.apply(item);
            if(!toReorder.contains(itemId)) {
                newList.add(item);
            }
            if(itemId.equals(afterId)) {
                newList.addAll(items);
                added = true;
            }
        }
        if(!added) {
            newList.addAll(items);
        }
        return newList;
    }
}
