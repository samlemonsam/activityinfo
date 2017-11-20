package org.activityinfo.ui.client.chrome;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.ResizeContainer;
import com.sencha.gxt.widget.core.client.menu.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.OfflineStatus;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.table.view.ConfirmDialog;

import java.util.Set;

/**
 * Displays the current connection status.
 */
public class OfflineMenu implements IsWidget {

    private InlineHTML icon;
    private final MenuItem pendingChangesItem;
    private final MenuItem syncNowItem;
    private final MenuItem clearMenuItem;
    private OfflineStore offlineStore;


    public OfflineMenu(HttpBus bus, OfflineStore offlineStore) {
        this.offlineStore = offlineStore;

        pendingChangesItem = new MenuItem();
        pendingChangesItem.setHideOnClick(false);

        syncNowItem = new MenuItem(I18N.CONSTANTS.syncNow());
        syncNowItem.addSelectionHandler(event -> offlineStore.syncChanges());

        CheckMenuItem syncAutoItem = new CheckMenuItem(I18N.CONSTANTS.syncAutomatically());

        clearMenuItem = new MenuItem(I18N.CONSTANTS.clearOfflineMode());
        clearMenuItem.addSelectionHandler(this::confirmOfflineDelete);

        Menu menu = new Menu();
        menu.add(pendingChangesItem);
        menu.add(new SeparatorMenuItem());
        menu.add(syncNowItem);
        menu.add(syncAutoItem);
        menu.add(new SeparatorMenuItem());
        menu.add(clearMenuItem);


        icon = new InlineHTML(ChromeBundle.BUNDLE.offlineIcon().getText());
        icon.addStyleName(ChromeBundle.BUNDLE.style().appBarButton());
        icon.setVisible(false);
        icon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                menu.show(icon.getElement(), new Style.AnchorAlignment(Style.Anchor.TOP, Style.Anchor.BOTTOM));
            }
        });

        offlineStore.getOfflineForms().subscribe(this::onOfflineFormSetChanged);
        offlineStore.getStatus().subscribe(this::onStatusChanged);

    }

    private void onOfflineFormSetChanged(Observable<Set<ResourceId>> offlineForms) {
        // Only show the offline menu if the user has enabled at
        // least one form.
        if(offlineForms.isLoaded()) {
            boolean visible = !offlineForms.get().isEmpty();
            if(visible != icon.isVisible()) {
                icon.setVisible(visible);
                ResizeContainer parent = (ResizeContainer) icon.getParent();
                parent.forceLayout();
            }

        }
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


    private void confirmOfflineDelete(SelectionEvent<Item> itemSelectionEvent) {

        ConfirmDialog.confirm(new ConfirmDialog.Action() {
            @Override
            public ConfirmDialog.Messages getConfirmationMessages() {
                return new ConfirmDialog.Messages(
                    I18N.CONSTANTS.clearOfflineMode(),
                    I18N.CONSTANTS.confirmClearOfflineMode(),
                    I18N.CONSTANTS.ok());
            }

            @Override
            public ConfirmDialog.Messages getProgressMessages() {
                return new ConfirmDialog.Messages(
                    I18N.CONSTANTS.clearOfflineMode(),
                    I18N.CONSTANTS.confirmClearOfflineMode(),
                    I18N.CONSTANTS.ok());
            }

            @Override
            public ConfirmDialog.Messages getFailureMessages() {
                return new ConfirmDialog.Messages(
                    I18N.CONSTANTS.clearOfflineMode(),
                    I18N.CONSTANTS.errorUnexpectedOccured(),
                    I18N.CONSTANTS.retry());
            }

            @Override
            public Promise<Void> execute() {
                return offlineStore.clear();
            }

            @Override
            public void onComplete() {

            }
        });
    }
}
