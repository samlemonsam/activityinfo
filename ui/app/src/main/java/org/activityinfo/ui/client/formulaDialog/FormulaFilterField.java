package org.activityinfo.ui.client.formulaDialog;

import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;

/**
 * Created by alex on 3-2-17.
 */
public class FormulaFilterField extends StoreFilterField<FormulaElement> {
    @Override
    protected boolean doSelect(Store<FormulaElement> store, FormulaElement parent, FormulaElement item, String filter) {
        return item.matches(filter);
    }
}
