package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.store.OfflineStatus;


public class OfflineStatusButton implements IsWidget {

    private TextButton button;

    public OfflineStatusButton(FormStore formStore, ResourceId formId) {
        button = new TextButton();
        Observable<OfflineStatus> offlineStatus = formStore.getOfflineStatus(formId);
        offlineStatus.subscribe(this::statusChanged);

    }

    private void statusChanged(Observable<OfflineStatus> status) {
        if(status.isLoading()) {
            button.setEnabled(false);
            button.setText(I18N.CONSTANTS.loading());
        } else {
            button.setEnabled(true);
            if(status.get().isEnabled()) {
                button.setText("Offline mode enabled");
            } else {
                button.setText("Make form available offline");
            }
        }
    }

    @Override
    public Widget asWidget() {
        return button;
    }
}
