package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LookupViewModel {

    private Map<LookupKey, LookupField> fieldMap = new HashMap<>();
    private List<LookupField> fields = new ArrayList<>();

    public LookupViewModel(FormStore formStore, LookupKeySet keySet) {
        for (LookupKey lookupKey : keySet.getKeys()) {
            LookupField field = new LookupField(formStore, lookupKey);
            fields.add(field);
            fieldMap.put(lookupKey, field);
        }
    }

    public List<LookupField> getFields() {
        return fields;
    }

    public LookupField getField(int index) {
        return fields.get(index);
    }


}


