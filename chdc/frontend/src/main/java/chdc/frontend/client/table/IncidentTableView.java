package chdc.frontend.client.table;

import chdc.frontend.client.entry.DataEntryPlace;
import chdc.frontend.client.i18n.ChdcLabels;
import chdc.frontend.client.theme.*;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.chrome.HasTitle;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.view.TableGrid;

import java.util.logging.Logger;

/**
 * Table view that occupies the full width, with an action bar below
 */
public class IncidentTableView implements IsWidget, HasTitle {

    private static final Logger LOGGER = Logger.getLogger(IncidentTableView.class.getName());

    private final MainContainer main;
    private final FlowLayoutContainer gridContainer;
    private TableViewModel viewModel;
    private FormStore formStore;
    private TableGrid grid;

    public IncidentTableView(FormStore formStore, final TableViewModel viewModel) {
        this.formStore = formStore;

        gridContainer = new FlowLayoutContainer() {
            @Override
            protected void onAttach() {
                super.onAttach();
                viewModel.getEffectiveTable().subscribe(tm -> effectiveModelChanged());
            }
        };
        this.viewModel = viewModel;
        gridContainer.addStyleName("gridholder");

        FlowLayoutContainer mainContent = new FlowLayoutContainer();
        mainContent.setStyleName("maincontent");
        mainContent.add(gridContainer);

        // Action bar

        FlowLayoutContainer tableActions = new FlowLayoutContainer();
        tableActions.add(new IconLinkButton(Icon.PLUS, ChdcLabels.LABELS.newRow(),
                new DataEntryPlace(viewModel.getFormId()).toUri()));

        ActionBar actionBar = new ActionBar();
        actionBar.addShortcut(new QuickSearchForm());
        actionBar.addShortcut(tableActions);

        // Main

        main = new MainContainer();
        main.add(mainContent);
        main.add(actionBar);

    }


    private void effectiveModelChanged() {
        if(viewModel.getEffectiveTable().isLoading()) {
            this.gridContainer.mask();
        } else {
            this.gridContainer.unmask();

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

    }

    private void updateGrid(EffectiveTableModel effectiveTableModel) {

        // If the grid is already displayed, try to update without
        // destorying everything
        if(grid != null && grid.updateView(effectiveTableModel)) {
            return;
        }

        if(grid != null) {
            gridContainer.remove(grid);
        }

        LOGGER.info("mainContent = {" + main.getOffsetWidth() + " x " + main.getOffsetHeight() + "}");

        grid = new TableGrid(effectiveTableModel, viewModel.getColumnSet(), viewModel);
        grid.addSelectionChangedHandler(event -> {
            if(!event.getSelection().isEmpty()) {
                RecordRef ref = event.getSelection().get(0);
                viewModel.select(ref);
            }
        });
        grid.asWidget().setPixelSize(main.getOffsetWidth(), main.getOffsetHeight());
        gridContainer.add(grid);
    }

    @Override
    public Widget asWidget() {
        return main;
    }

    @Override
    public Observable<String> getTitle() {
        throw new UnsupportedOperationException("TODO");
    }
}
