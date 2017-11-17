package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.store.offline.FormOfflineStatus;


public class OfflineStatusButton implements IsWidget {

    private TextButton button;
    private final Observable<FormOfflineStatus> status;
    private FormStore formStore;
    private ResourceId formId;

    public OfflineStatusButton(FormStore formStore, ResourceId formId) {
        this.formStore = formStore;
        this.formId = formId;
        button = new TextButton();
        status = formStore.getOfflineStatus(formId);
        status.subscribe(this::statusChanged);
        button.addSelectHandler(this::clicked);

    }

    private void clicked(SelectEvent event) {
        if(status.isLoaded()) {
            if(status.get().isEnabled()) {
                formStore.setFormOffline(formId, false);
            } else {
                formStore.setFormOffline(formId, true);
            }
        }
    }

    private void statusChanged(Observable<FormOfflineStatus> status) {
        if(status.isLoading()) {
            button.setEnabled(false);
            button.setText(I18N.CONSTANTS.loading());
        } else {
            button.setEnabled(true);
            if(!status.get().isEnabled()) {
                button.setText(I18N.CONSTANTS.makeAvailableOffline());
            } else if(status.get().isCached()) {
                button.setText(I18N.CONSTANTS.availableOffline());
            } else {
                button.setText(I18N.CONSTANTS.downloadInProgress());
            }
        }
    }

    @Override
    public Widget asWidget() {
        return button;
    }
}
