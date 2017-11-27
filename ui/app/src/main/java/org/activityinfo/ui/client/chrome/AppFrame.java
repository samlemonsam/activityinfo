package org.activityinfo.ui.client.chrome;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.AppCache;
import org.activityinfo.ui.client.store.http.HttpStore;
import org.activityinfo.ui.client.store.offline.OfflineStore;

/**
 * The outer application frame that houses the login menu, connection indicator, etc.
 */
public class AppFrame implements IsWidget {

    private BorderLayoutContainer container;
    private OfflineMenu offlineMenu;
    private final HTML titleHtml;

    private Subscription titleSubscription;

    public AppFrame(AppCache appCache, HttpStore httpStore, OfflineStore offlineStore) {
        ChromeBundle.BUNDLE.style().ensureInjected();

        HTML logoLink = new HTML(ChromeBundle.BUNDLE.logoLink().getText());

        titleHtml = new HTML("ActivityInfo");
        titleHtml.addStyleName(ChromeBundle.BUNDLE.style().appTitle());

        BoxLayoutContainer.BoxLayoutData titleLayout = new BoxLayoutContainer.BoxLayoutData();
        titleLayout.setFlex(1);
        titleLayout.setMargins(new Margins(0, 0, 0, 10));

        HBoxLayoutContainer appBar = new HBoxLayoutContainer(HBoxLayoutContainer.HBoxLayoutAlign.MIDDLE);
        appBar.setEnableOverflow(false);
        appBar.addStyleName(ChromeBundle.BUNDLE.style().appBar());
        appBar.add(logoLink);
        appBar.add(titleHtml, titleLayout);
        appBar.add(new ConnectionIcon(httpStore.getHttpBus()));
        appBar.add(new OfflineMenu(httpStore.getHttpBus(), offlineStore));
        appBar.add(new LocaleSelector());
        appBar.add(new SystemMenu(appCache));

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

                if(titleSubscription != null) {
                    titleSubscription.unsubscribe();
                    titleSubscription = null;
                }

                Observable<String> title;
                if (w instanceof HasTitle) {
                    title = ((HasTitle) w).getTitle();
                } else {
                    title = Observable.loading();
                }
                titleSubscription = title.subscribe(AppFrame.this::titleChanged);
            }
        };
    }

    private void titleChanged(Observable<String> title) {
        if(title.isLoading()) {
            titleHtml.setText("ActivityInfo");
            Window.setTitle("ActivityInfo");
        } else {
            titleHtml.setText(title.get());
            Window.setTitle(title.get() + " - ActivityInfo");
        }
    }

}