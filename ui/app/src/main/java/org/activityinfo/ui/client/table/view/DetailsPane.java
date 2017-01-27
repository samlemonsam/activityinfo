package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by alex on 20-1-17.
 */
public class DetailsPane implements IsWidget {

    private final HTMLPanel panel;

    public DetailsPane() {
        this.panel = new HTMLPanel("Testing");
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
