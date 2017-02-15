package org.activityinfo.ui.client.formulaDialog;

import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;
import org.activityinfo.i18n.shared.I18N;

public class FormulaElementFilter extends StoreFilterField<FormulaElement> {

    public FormulaElementFilter() {
        setEmptyText(I18N.CONSTANTS.search());
    }

    @Override
    protected boolean doSelect(Store<FormulaElement> store, FormulaElement parent, FormulaElement item, String filter) {
        return item.getLabel().toLowerCase().contains(filter.toLowerCase());
    }
}
