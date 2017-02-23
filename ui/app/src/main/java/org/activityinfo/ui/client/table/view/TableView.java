package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.input.view.FormDialog;
import org.activityinfo.ui.client.table.model.TableModel;

/**
 * Displays a Form as a Table
 */
public class TableView implements IsWidget {

    private TableModel model;
    private ContentPanel panel;
    private final BorderLayoutContainer container;

    private ToolBar toolBar;
    private TableGrid grid;

    private VerticalLayoutContainer center;

    private DetailsPane detailsPane;

    private Subscription subscription;

    public TableView(final TableModel model) {
        this.model = model;


        this.detailsPane = new DetailsPane();

        TextButton newButton = new TextButton("New Record");
        newButton.addSelectHandler(this::onNewRecordClicked);


        this.toolBar = new ToolBar();
        toolBar.add(newButton);

        center = new VerticalLayoutContainer();
        center.add(toolBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        this.container = new BorderLayoutContainer();
        this.container.setEastWidget(detailsPane, new BorderLayoutContainer.BorderLayoutData(150));
        this.container.setCenterWidget(center);

        this.panel = new ContentPanel() {

            @Override
            protected void onAttach() {
                super.onAttach();
                subscription = model.getEffectiveTable().subscribe(observable -> effectiveModelChanged());
            }

            @Override
            protected void onDetach() {
                super.onDetach();
                subscription.unsubscribe();
            }
        };
        this.panel.setHeading(I18N.CONSTANTS.loading());
        this.panel.add(container);
    }

    private void onNewRecordClicked(SelectEvent event) {
        FormDialog dialog = new FormDialog(model.getFormStore(), model.getFormId());
        dialog.show();
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
                center.add(grid, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
                center.forceLayout();

            } else {
                grid.update(model.getEffectiveTable());
            }
        }
    }
}
