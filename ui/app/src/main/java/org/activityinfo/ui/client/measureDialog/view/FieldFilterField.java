package org.activityinfo.ui.client.measureDialog.view;

import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;


public class FieldFilterField extends StoreFilterField<FieldElement> {
    @Override
    protected boolean doSelect(Store<FieldElement> store, FieldElement parent, FieldElement item, String filter) {
        return parent.getLabel().toLowerCase().contains(filter.toLowerCase());
    }
}
