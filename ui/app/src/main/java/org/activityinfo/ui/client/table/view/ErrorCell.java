package org.activityinfo.ui.client.table.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import java.util.Collections;
import java.util.Set;

/**
 * Shows an error in formula
 */
public class ErrorCell implements Cell<String> {
    @Override
    public boolean dependsOnSelection() {
        return false;
    }

    @Override
    public Set<String> getConsumedEvents() {
        return Collections.emptySet();
    }

    @Override
    public boolean handlesSelection() {
        return false;
    }

    @Override
    public boolean isEditing(Context context, Element parent, String value) {
        return false;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {

    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        sb.appendEscaped("#VALUE!");
    }

    @Override
    public boolean resetFocus(Context context, Element parent, String value) {
        return false; // = focus not taken
    }

    @Override
    public void setValue(Context context, Element parent, String value) {
    }
}
