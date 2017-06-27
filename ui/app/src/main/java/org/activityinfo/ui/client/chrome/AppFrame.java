package org.activityinfo.ui.client.chrome;

import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.ui.client.store.http.HttpStore;
import org.activityinfo.ui.client.store.offline.OfflineStore;

/**
 * The outer application frame that houses the login menu, connection indicator, etc.
 */
public class AppFrame implements IsWidget {

    private BorderLayoutContainer container;
    private OfflineMenu offlineMenu;

    public AppFrame(HttpStore httpStore, OfflineStore offlineStore) {
        ChromeBundle.BUNDLE.style().ensureInjected();

        HTML logoLink = new HTML(ChromeBundle.BUNDLE.logoLink().getText());

        ToolBar appBar = new ToolBar();
        appBar.add(logoLink);
        appBar.add(new FillToolItem());
        appBar.add(new ConnectionIcon(httpStore.getHttpBus()));
        appBar.add(new OfflineMenu(httpStore.getHttpBus(), offlineStore));
        appBar.add(new LocaleSelector());

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