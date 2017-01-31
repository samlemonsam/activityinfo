package org.activityinfo.ui.client.chrome;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import org.activityinfo.ui.client.http.HttpBus;

/**
 * The outer application frame that houses the login menu, connection indicator, etc.
 */
public class AppFrame implements IsWidget {

    interface AppFrameUiBinder extends UiBinder<BorderLayoutContainer, AppFrame> {
    }

    private static AppFrameUiBinder ourUiBinder = GWT.create(AppFrameUiBinder.class);


    BorderLayoutContainer container;


    @UiField
    ConnectionIndicator connectionIndicator;

    public AppFrame(HttpBus bus) {
        container = ourUiBinder.createAndBindUi(this);
        connectionIndicator.setStatus(bus.getStatus());
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    public AcceptsOneWidget getDisplayWidget() {
        return new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget w) {
                container.setCenterWidget(w);
                container.forceLayout();
            }
        };
    }
}