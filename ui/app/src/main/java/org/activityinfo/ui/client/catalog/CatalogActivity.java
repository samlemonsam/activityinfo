package org.activityinfo.ui.client.catalog;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.ui.client.catalog.view.CatalogView;
import org.activityinfo.ui.client.store.FormStore;

public class CatalogActivity extends AbstractActivity {

    private final FormStore formStore;
    private final CatalogPlace place;

    public CatalogActivity(FormStore formStore, CatalogPlace place) {
        this.formStore = formStore;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        CatalogView view = new CatalogView(formStore, place.getParentId());
        panel.setWidget(view);
    }
}
