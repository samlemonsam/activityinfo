package org.activityinfo.ui.client.table.view;

import com.google.common.base.Optional;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.observable.Observable;


public class DetailsPane implements IsWidget {

    private final HTML panel;

    public DetailsPane(TableViewModel viewModel) {

        this.panel = new HTML("Testing");
        this.panel.addStyleName(TableBundle.INSTANCE.style().detailPane());

        Observable<DetailsRenderer> renderer = viewModel.getFormTree().transform(DetailsRenderer::new);
        Observable<Optional<FormRecord>> selection = viewModel.getSelectedRecord();
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
