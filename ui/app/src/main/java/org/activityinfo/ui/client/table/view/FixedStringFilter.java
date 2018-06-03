package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;

public class FixedStringFilter extends StringFilter<Integer> {
    /**
     * Creates a string filter for the specified value provider. See {@link Filter#Filter(ValueProvider)} for more
     * information.
     *
     * @param valueProvider the value provider
     */
    public FixedStringFilter(ValueProvider<? super Integer, String> valueProvider) {
        super(valueProvider);
    }


    /**
     * AI-1919 DO NOT trigger update on key stroke. This causes the filter input to loose
     * focus and the next key stroke closes the filter menu.
     */
    protected void onFieldKeyUp(Event event) {
        int key = event.getKeyCode();
        if (key == KeyCodes.KEY_ENTER && field.isValid()) {
            event.stopPropagation();
            event.preventDefault();
            menu.hide(true);
        }
    }
}
