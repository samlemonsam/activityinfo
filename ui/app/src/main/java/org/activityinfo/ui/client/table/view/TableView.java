package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.table.model.TableModel;

/**
 * Displays a Form as a Table
 */
public class TableView implements IsWidget {

    private TableModel model;
    private ContentPanel panel;
    private final BorderLayoutContainer container;

    private TableGrid grid;
    private DetailsPane detailsPane;

    public TableView(final TableModel model) {
        this.model = model;
        this.panel = new ContentPanel() {

            @Override
            protected void onAttach() {
                super.onAttach();
                model.getEffectiveTable().subscribe(observable -> effectiveModelChanged());
            }

            @Override
            protected void onDetach() {
                super.onDetach();

            }
        };
        this.panel.setHeading(I18N.CONSTANTS.loading());

        this.container = new BorderLayoutContainer();
        this.detailsPane = new DetailsPane();

        this.container.setEastWidget(detailsPane, new BorderLayoutContainer.BorderLayoutData(150));

        this.panel.add(container);
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
                container.setCenterWidget(grid);
                container.forceLayout();

            } else {
                grid.update(model.getEffectiveTable());
            }
        }
    }
}
