package org.activityinfo.ui.client.analysis.model;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.List;

public final class ImmutableLists {

    public static <T> List<T> update(List<T> list, T updatedItem, Function<T, String> keyProvider) {

        String updatedId = keyProvider.apply(updatedItem);

        List<T> newList = new ArrayList<>(list.size());
        boolean updated = false;
        for (T item : newList) {
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

}
