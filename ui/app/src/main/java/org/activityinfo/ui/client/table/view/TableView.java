package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.input.view.FormDialog;
import org.activityinfo.ui.client.table.viewModel.TableViewModel;

/**
 * Displays a Form as a Table
 */
public class TableView implements IsWidget {

    private TableViewModel viewModel;
    private ContentPanel panel;
    private final BorderLayoutContainer container;

    private ToolBar toolBar;
    private TableGrid grid;

    private VerticalLayoutContainer center;

    private Subscription subscription;

    public TableView(final TableViewModel viewModel) {

        TableBundle.INSTANCE.style().ensureInjected();

        this.viewModel = viewModel;

        TextButton newButton = new TextButton("New Record");
        newButton.addSelectHandler(this::onNewRecordClicked);


        this.toolBar = new ToolBar();
        toolBar.add(newButton);

        center = new VerticalLayoutContainer();
        center.add(toolBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        this.container = new BorderLayoutContainer();
        BorderLayoutContainer.BorderLayoutData east = new BorderLayoutContainer.BorderLayoutData(150);
        east.setSplit(true);
        int margins = 8;
        east.setMargins(new Margins(0, 0, 0, margins));
        this.container.setEastWidget(new SidePanel(viewModel), east);
        this.container.setCenterWidget(center);

        this.panel = new ContentPanel() {

            @Override
            protected void onAttach() {
                super.onAttach();
                subscription = viewModel.getEffectiveTable().subscribe(observable -> effectiveModelChanged());
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
        FormDialog dialog = new FormDialog(viewModel.getFormStore(), viewModel.getFormId());
        dialog.show();
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    private void effectiveModelChanged() {
        if(viewModel.getEffectiveTable().isLoading()) {
            this.panel.mask();
        } else {
            this.panel.setHeading(viewModel.getEffectiveTable().get().getFormLabel());
            this.panel.unmask();
            if(grid == null) {
                grid = new TableGrid(viewModel, viewModel.getEffectiveTable().get());
                center.add(grid, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
                center.forceLayout();

            } else {
                grid.update(viewModel.getEffectiveTable());
            }
        }
    }
}
