package org.activityinfo.ui.client.table.view;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormHistoryEntry;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

import java.util.Collections;
import java.util.List;

public class HistoryPane implements IsWidget {

    private final HTML panel;
    private final HistoryRenderer renderer = new HistoryRenderer();

    public HistoryPane(FormStore formStore, TableViewModel viewModel) {
        this.panel = new HTML("Testing");
        this.panel.addStyleName(TableBundle.INSTANCE.style().detailPane());

        Observable<List<FormHistoryEntry>> history = viewModel.getSelectedRecordRef().join(ref -> {
            if (ref.isPresent()) {
                return formStore.getFormRecordHistory(ref.get());
            } else {
                return Observable.just(Collections.emptyList());
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
