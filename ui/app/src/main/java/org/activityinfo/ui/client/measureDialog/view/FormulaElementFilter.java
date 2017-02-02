package org.activityinfo.ui.client.measureDialog.view;

import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;

public class FormulaElementFilter extends StoreFilterField<FormulaElement> {
    @Override
    protected boolean doSelect(Store<FormulaElement> store, FormulaElement parent, FormulaElement item, String filter) {
        return item.getLabel().toLowerCase().contains(filter.toLowerCase());
    }
}
