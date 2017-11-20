package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;

/**
 * Widget that fills an area with the message that the user
 * does not have access to this component
 */
public class ForbiddenWidget implements IsWidget {


    private final CenterLayoutContainer container;

    public ForbiddenWidget() {
        container = new CenterLayoutContainer();
        container.add(new Label("You do not have permission to view this form"));
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
