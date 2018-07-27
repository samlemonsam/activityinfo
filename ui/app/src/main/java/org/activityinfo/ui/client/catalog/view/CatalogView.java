/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

        treeView = new CatalogTreeView(formStore, parentId,
            entry -> !entry.getId().equals("geodb"));

        treeView.setIncludeSubForms(false);

        container = new VerticalLayoutContainer();
        container.add(bar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        container.add(treeView, new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        treeView.getSelectedEntry().subscribe(this::selectionChanged);

    }

    private void openTable(SelectEvent event) {
        if(treeView.getSelectedEntry().isLoaded() && treeView.getSelectedEntry().get().isPresent()) {
            CatalogEntry catalogEntry = treeView.getSelectedEntry().get().get();
            if(catalogEntry.getType() == CatalogEntryType.FORM) {
                String url = Window.Location.createUrlBuilder().setHash("table/" + catalogEntry.getId()).buildString();
                Window.open(url, null, null);
            } else if(catalogEntry.getType() == CatalogEntryType.ANALYSIS) {
                String url = Window.Location.createUrlBuilder().setHash("analysis/" + catalogEntry.getId()).buildString();
                Window.open(url, null, null);
            }
        }
    }

    private void selectionChanged(Observable<Optional<CatalogEntry>> selection) {

        boolean hasSelection = selection.isLoaded() && selection.get().isPresent();
        CatalogEntry catalogEntry = null;

        if(hasSelection) {
            catalogEntry = selection.get().get();
        }

        openTable.setEnabled(hasSelection &&
            (catalogEntry.getType() == CatalogEntryType.FORM || catalogEntry.getType() == CatalogEntryType.ANALYSIS));
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
