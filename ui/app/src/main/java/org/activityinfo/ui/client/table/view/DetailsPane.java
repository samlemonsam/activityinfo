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

import com.google.common.base.Optional;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.RecordTreeLoader;


public class DetailsPane implements IsWidget {

    private final HTML panel;

    public DetailsPane(TableViewModel viewModel) {

        this.panel = new HTML("Testing");
        this.panel.addStyleName(TableBundle.INSTANCE.style().detailPane());

        Observable<DetailsRenderer> renderer = viewModel.getFormTree().transform(DetailsRenderer::new);
        Observable<Optional<RecordTree>> selection = viewModel.getSelectedRecordTree();
        Observable<SafeHtml> html = Observable.transform(renderer, selection, (detailsRenderer, selected) -> {
            if(selected.isPresent()) {
                return detailsRenderer.render(selected.get());
            } else {
                return detailsRenderer.renderNoSelection();
            }
        });

        html.subscribe(observable -> {
            if(observable.isLoaded()) {
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
