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
package org.activityinfo.ui.client.table.view;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

import java.util.Collections;

public class HistoryPane implements IsWidget {

    private final HTML panel;
    private final HistoryRenderer renderer = new HistoryRenderer();

    public HistoryPane(FormStore formStore, TableViewModel viewModel) {
        this.panel = new HTML("Testing");
        this.panel.addStyleName(TableBundle.INSTANCE.style().detailPane());

        Observable<RecordHistory> history = viewModel.getSelectedRecordRef().join(ref -> {
            if (ref.isPresent()) {
                return formStore.getFormRecordHistory(ref.get());
            } else {
                return Observable.just(RecordHistory.create(Collections.emptyList()));
            }
        });
        Observable<SafeHtml> html = history.transform((h) -> renderer.render(h));
        html.subscribe(observable -> {
            if (observable.isLoaded()) {
                panel.setHTML(observable.get());
            } else {
                panel.setHTML(I18N.CONSTANTS.loading());
            }
        });
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
