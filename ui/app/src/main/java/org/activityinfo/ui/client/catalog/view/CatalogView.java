package org.activityinfo.ui.client.catalog.view;

import com.google.common.base.Optional;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.chrome.HasTitle;
import org.activityinfo.ui.client.measureDialog.view.CatalogTreeView;
import org.activityinfo.ui.client.store.FormStore;

public class CatalogView implements IsWidget, HasTitle {

    private final VerticalLayoutContainer container;
    private final TextButton openTable;
    private final TextButton newReport;
    private final CatalogTreeView treeView;

    public CatalogView(FormStore formStore, Optional<String> parentId) {

        openTable = new TextButton(I18N.CONSTANTS.openTable());
        openTable.addSelectHandler(this::openTable);
        newReport = new TextButton(I18N.CONSTANTS.createNewReport());

        ToolBar bar = new ToolBar();
        bar.add(openTable);
        bar.add(newReport);

        treeView = new CatalogTreeView(formStore, parentId, entry -> !entry.getId().equals("geodb"));

        container = new VerticalLayoutContainer();
        container.add(bar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        container.add(treeView, new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        treeView.getSelectedEntry().subscribe(this::selectionChanged);

    }

    private void openTable(SelectEvent event) {
        if(treeView.getSelectedFormId().isLoaded() && treeView.getSelectedFormId().get().isPresent()) {
            ResourceId formId = treeView.getSelectedFormId().get().get();
            String url = Window.Location.createUrlBuilder().setHash("table/" + formId.asString()).buildString();
            Window.open(url, null, null);
        }
    }

    private void selectionChanged(Observable<Optional<CatalogEntry>> selection) {

        boolean hasSelection = selection.isLoaded() && selection.get().isPresent();
        CatalogEntry catalogEntry = null;

        if(hasSelection) {
            catalogEntry = selection.get().get();
        }

        openTable.setEnabled(hasSelection && catalogEntry.getType() == CatalogEntryType.FORM);
        newReport.setEnabled(hasSelection && catalogEntry.getType() == CatalogEntryType.FOLDER);
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    @Override
    public Observable<String> getTitle() {
        return Observable.just("ActivityInfo");
    }
}
