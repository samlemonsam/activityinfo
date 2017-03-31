package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.table.viewModel.EffectiveTableModel;
import org.activityinfo.ui.client.table.viewModel.TableViewModel;

/**
 * Holds a sub form grid
 */
public class SubFormGrid extends VerticalLayoutContainer {

    private final Observable<EffectiveTableModel> tableModel;
    private TableViewModel viewModel;
    private ResourceId subFormId;
    private Subscription subscription;

    private TableGrid grid;

    public SubFormGrid(TableViewModel viewModel, ResourceId subFormId) {
        this.viewModel = viewModel;
        this.subFormId = subFormId;
        this.tableModel = viewModel.getEffectiveSubTable(subFormId);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        subscription = tableModel.subscribe(this::onModelChanged);
    }

    private void onModelChanged(Observable<EffectiveTableModel> model) {
        if(model.isLoaded()) {
            if(grid == null) {
                grid = new TableGrid(model.get());
                add(grid, new VerticalLayoutData(1, 1));
            }
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        subscription.unsubscribe();
    }
}
