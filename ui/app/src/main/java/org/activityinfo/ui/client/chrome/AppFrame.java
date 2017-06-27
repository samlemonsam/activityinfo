package org.activityinfo.ui.client.chrome;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.ui.client.store.http.HttpBus;

/**
 * The outer application frame that houses the login menu, connection indicator, etc.
 */
public class AppFrame implements IsWidget {


    private BorderLayoutContainer container;
    private ConnectionIndicator connectionIndicator;

    public AppFrame(HttpBus bus) {

        HTML logoLink = new HTML(ChromeBundle.BUNDLE.logoLink().getText());
        ConnectionIndicator connectionIndicator = new ConnectionIndicator(bus);

        ToolBar appBar = new ToolBar();
        appBar.add(logoLink);
        appBar.add(new FillToolItem());
        appBar.add(connectionIndicator);
        appBar.add(new LanguageSelector());

        container = new BorderLayoutContainer();
        container.setNorthWidget(appBar, new BorderLayoutContainer.BorderLayoutData(50));

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