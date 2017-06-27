package org.activityinfo.ui.client.chrome;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.OfflineStatus;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.store.offline.PendingStatus;
import org.activityinfo.ui.client.store.offline.SnapshotStatus;

/**
 * Displays the current connection status.
 */
public class OfflineMenu implements IsWidget {

    private InlineHTML icon;
    private final MenuItem pendingChangesItem;
    private final MenuItem syncNowItem;


    public OfflineMenu(HttpBus bus, OfflineStore offlineStore) {

        pendingChangesItem = new MenuItem();
        pendingChangesItem.setHideOnClick(false);

        syncNowItem = new MenuItem(I18N.CONSTANTS.syncNow());
        syncNowItem.addSelectionHandler(event -> offlineStore.syncChanges());

        CheckMenuItem syncAutoItem = new CheckMenuItem(I18N.CONSTANTS.syncAutomatically());

        Menu menu = new Menu();
        menu.add(pendingChangesItem);
        menu.add(new SeparatorMenuItem());
        menu.add(syncNowItem);
        menu.add(syncAutoItem);


        icon = new InlineHTML(ChromeBundle.BUNDLE.offlineIcon().getText());
        icon.addStyleName(ChromeBundle.BUNDLE.style().appBarButton());
        icon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                menu.show(icon.getElement(), new Style.AnchorAlignment(Style.Anchor.TOP, Style.Anchor.BOTTOM));
            }
        });

        offlineStore.getStatus().subscribe(this::onStatusChanged);

    }

    @Override
    public Widget asWidget() {
        return icon;
    }


    private void onStatusChanged(Observable<OfflineStatus> observable) {
        if(observable.isLoaded()) {
            OfflineStatus status = observable.get();
            updatePendingCountIcon(status.getPendingChangeCount());

            toggleClass(ChromeBundle.BUNDLE.style().pending(), status.getPendingChangeCount() > 0);
            toggleClass(ChromeBundle.BUNDLE.style().synced(), status.isSynced());
        }
    }

    private void updatePendingCountIcon(int count) {
        Element countElement = Document.get().getElementById("pending-count");
        countElement.setInnerText(Integer.toString(count));

        pendingChangesItem.setText(I18N.MESSAGES.pendingChanges(count));

    }


    private void toggleClass(String className, boolean add) {
        if(add) {
            icon.getElement().addClassName(className);
        } else {
            icon.getElement().removeClassName(className);
        }
    }

}
