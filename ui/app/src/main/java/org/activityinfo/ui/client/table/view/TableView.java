package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.input.view.FormDialog;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Displays a Form as a Table
 */
public class TableView implements IsWidget {

    public static final int MARGINS = 8;


    private TableViewModel viewModel;
    private ContentPanel panel;
    private final BorderLayoutContainer container;

    private ToolBar toolBar;
    private TableGrid grid;

    private VerticalLayoutContainer center;

    private Subscription subscription;

    private Dialog forbiddenDialog;
    private FormStore formStore;


    public TableView(FormStore formStore, final TableViewModel viewModel) {
        this.formStore = formStore;

        TableBundle.INSTANCE.style().ensureInjected();

        this.viewModel = viewModel;

        TextButton newButton = new TextButton("New Record");
        newButton.addSelectHandler(this::onNewRecordClicked);


        OfflineStatusButton offlineButton = new OfflineStatusButton(formStore, viewModel.getFormId());

        this.toolBar = new ToolBar();
        toolBar.add(newButton);
        toolBar.add(offlineButton);

        center = new VerticalLayoutContainer();
        center.add(toolBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        this.container = new BorderLayoutContainer();


        SidePanel sidePanel = new SidePanel(viewModel);
        BorderLayoutContainer.BorderLayoutData sidePaneLayout = new BorderLayoutContainer.BorderLayoutData(150);
        sidePaneLayout.setSplit(true);
        sidePaneLayout.setMargins(new Margins(0, 0, 0, MARGINS));

//        SubFormPane subFormPane = new SubFormPane(viewModel);
//        BorderLayoutContainer.BorderLayoutData subFormPaneLayout = new BorderLayoutContainer.BorderLayoutData(150);
//        subFormPaneLayout.setSplit(true);
//        subFormPaneLayout.setMargins(new Margins(0, 0, 0, MARGINS));

        this.container.setEastWidget(sidePanel, sidePaneLayout);
//        this.container.setSouthWidget(subFormPane, subFormPaneLayout);
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
        FormDialog dialog = new FormDialog(formStore, viewModel.getFormId());
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
            this.panel.unmask();

            switch (viewModel.getEffectiveTable().get().getRootFormState()) {
                case FORBIDDEN:
                case DELETED:
                    showErrorState(viewModel.getEffectiveTable().get().getRootFormState());
                    break;
                case VALID:
                    updateGrid(viewModel.getEffectiveTable().get());
                    break;
            }
        }
    }

    private void showErrorState(FormTree.State rootFormState) {
        forbiddenDialog = new Dialog();
        forbiddenDialog.setPixelSize(300, 250);
        forbiddenDialog.setHeading("Forbidden");
        forbiddenDialog.add(new Label("You do not have permission to view this form."));
        forbiddenDialog.setModal(true);
        forbiddenDialog.show();
    }

    private void updateGrid(EffectiveTableModel effectiveTableModel) {

        if(forbiddenDialog != null) {
            forbiddenDialog.hide();
        }

        this.panel.setHeading(effectiveTableModel.getFormLabel());
        if(grid == null) {
            grid = new TableGrid(effectiveTableModel);
            grid.addSelectionChangedHandler(event -> {
                if(!event.getSelection().isEmpty()) {
                    RecordRef ref = event.getSelection().get(0);
                    viewModel.select(ref);
                }
            });
            center.add(grid, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
            center.forceLayout();

        } else {
            grid.update(viewModel.getEffectiveTable());
        }
    }
}
