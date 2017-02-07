package org.activityinfo.ui.client.measureDialog.view;

import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;


public class FieldFilterField extends StoreFilterField<MeasureTreeNode> {
    @Override
    protected boolean doSelect(Store<MeasureTreeNode> store, MeasureTreeNode parent, MeasureTreeNode item, String filter) {
        return item.getLabel().toLowerCase().contains(filter.toLowerCase());
    }
}
