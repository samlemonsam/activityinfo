package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import java.util.Set;

/**
 * A cell for measures and dimensions
 */
public class PaneCell implements Cell<PaneItem> {
    @Override
    public boolean dependsOnSelection() {
        return false;
    }

    @Override
    public Set<String> getConsumedEvents() {
        return null;
    }

    @Override
    public boolean handlesSelection() {
        return false;
    }

    @Override
    public boolean isEditing(Context context, Element element, PaneItem paneItem) {
        return false;
    }

    @Override
    public void onBrowserEvent(Context context, Element element, PaneItem paneItem, NativeEvent nativeEvent, ValueUpdater<PaneItem> valueUpdater) {

    }

    @Override
    public void render(Context context, PaneItem paneItem, SafeHtmlBuilder safeHtmlBuilder) {

    }

    @Override
    public boolean resetFocus(Context context, Element element, PaneItem paneItem) {
        return false;
    }

    @Override
    public void setValue(Context context, Element element, PaneItem paneItem) {

    }
}
