package org.activityinfo.ui.client.table;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;

/**
 * Displays a Form as a Table
 */
public class TableView implements IsWidget {

    private TableModel model;
    private ContentPanel panel;
    private final BorderLayoutContainer borderContainer;

    private TableGrid grid;
    private DetailsPane detailsPane;

    public TableView(final TableModel model) {
        this.model = model;
        this.panel = new ContentPanel() {

            @Override
            protected void onAttach() {
                super.onAttach();
                model.getEffectiveTable().subscribe(new Observer<EffectiveTableModel>() {
                    @Override
                    public void onChange(Observable<EffectiveTableModel> observable) {
                        effectiveModelChanged();
                    }
                });
            }

            @Override
            protected void onDetach() {
                super.onDetach();

            }
        };
        this.panel.setHeading(I18N.CONSTANTS.loading());

        this.borderContainer = new BorderLayoutContainer();
        this.detailsPane = new DetailsPane();
        this.borderContainer.setEastWidget(detailsPane, new BorderLayoutContainer.BorderLayoutData(150));

        this.panel.add(borderContainer);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    private void effectiveModelChanged() {
        if(model.getEffectiveTable().isLoading()) {
            this.panel.mask();
        } else {
            this.panel.setHeading(model.getEffectiveTable().get().getFormLabel());
            this.panel.unmask();
            if(grid == null) {
                grid = new TableGrid(model.getEffectiveTable().get());
                borderContainer.setCenterWidget(grid);
                borderContainer.forceLayout();
            } else {
                grid.update(model.getEffectiveTable());
            }
        }
    }
}
