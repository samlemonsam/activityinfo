package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.SubscriptionSet;
import org.activityinfo.ui.client.chrome.HasTitle;
import org.activityinfo.ui.client.store.FormStore;

import java.util.logging.Logger;

/**
 * Displays a Form as a Table
 */
public class TableView implements IsWidget, HasTitle {

    public static final int MARGINS = 8;

    private static final Logger LOGGER = Logger.getLogger(TableView.class.getName());


    private TableViewModel viewModel;
    private ContentPanel panel;
    private final BorderLayoutContainer container;

    private TableGrid grid;

    private IsWidget errorWidget;

    private VerticalLayoutContainer center;

    private SubscriptionSet subscriptions = new SubscriptionSet();

    private FormStore formStore;
    private final SidePanel sidePanel;
    private SubFormPane subFormPane;


    public TableView(FormStore formStore, final TableViewModel viewModel) {
        this.formStore = formStore;

        TableBundle.INSTANCE.style().ensureInjected();

        this.viewModel = viewModel;

        TableToolBar toolBar = new TableToolBar(formStore, viewModel);

        center = new VerticalLayoutContainer();
        center.add(toolBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        this.container = new BorderLayoutContainer();

        sidePanel = new SidePanel(viewModel);
        BorderLayoutContainer.BorderLayoutData sidePaneLayout = new BorderLayoutContainer.BorderLayoutData(.3);
        sidePaneLayout.setSplit(true);
        sidePaneLayout.setMargins(new Margins(0, 0, 0, MARGINS));


        this.container.setEastWidget(sidePanel, sidePaneLayout);
        this.container.setCenterWidget(center);

        this.panel = new ContentPanel() {

            @Override
            protected void onAttach() {
                super.onAttach();
                LOGGER.info("TableView attaching...");
                subscriptions.add(viewModel.getEffectiveTable().subscribe(observable -> effectiveModelChanged()));
                subscriptions.add(viewModel.getFormTree().subscribe(tree -> formTreeChanged(tree)));
            }

            @Override
            protected void onDetach() {
                super.onDetach();
                LOGGER.info("TableView detaching...");
                subscriptions.unsubscribeAll();
            }
        };
        this.panel.setHeading(I18N.CONSTANTS.loading());
        this.panel.setHeaderVisible(false);
        this.panel.add(container);
    }

    public Observable<TableModel> getTableModel() {
        return this.viewModel.getTableModel();
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

    private void formTreeChanged(Observable<FormTree> tree) {
        if(tree.isLoaded()) {
            if (tree.get().hasSubForms()) {
                if(subFormPane == null) {
                    subFormPane = new SubFormPane(viewModel, tree.get());
                    BorderLayoutContainer.BorderLayoutData subFormPaneLayout = new BorderLayoutContainer.BorderLayoutData(0.3);
                    subFormPaneLayout.setSplit(true);
                    subFormPaneLayout.setMargins(new Margins(0, 0, 0, MARGINS));

                    this.container.setSouthWidget(subFormPane, subFormPaneLayout);
                    this.container.forceLayout();
                }
            } else {
                if(subFormPane != null) {
                    this.container.remove(subFormPane);
                    this.container.forceLayout();
                    subFormPane = null;
                }
            }
        }
    }

    private void showErrorState(FormTree.State rootFormState) {
        errorWidget = new ForbiddenWidget();

        panel.setWidget(errorWidget);
        panel.forceLayout();
    }

    private void updateGrid(EffectiveTableModel effectiveTableModel) {

        panel.setHeading(effectiveTableModel.getFormLabel());

        // If the grid is already displayed, try to update without
        // destorying everything
        if(grid != null && grid.updateView(effectiveTableModel)) {
            return;
        }

        if(grid != null) {
            center.remove(grid);
        }

        grid = new TableGrid(effectiveTableModel, viewModel.getColumnSet(), viewModel);
        grid.addSelectionChangedHandler(event -> {
            if(!event.getSelection().isEmpty()) {
                RecordRef ref = event.getSelection().get(0);
                viewModel.select(ref);
            }
        });
        center.add(grid, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
        center.forceLayout();


        // If we are transitioning from an error state, make the container with the
        // grid and sidebars is set
        if(!container.isAttached()) {
            panel.setWidget(container);
        }
        panel.forceLayout();
    }

    public void stop() {


    }

    @Override
    public Observable<String> getTitle() {
        return viewModel.getFormTree().transform(formTree -> {
            switch (formTree.getRootState()) {
                case VALID:
                    return formTree.getRootFormClass().getLabel();
                case DELETED:
                    return I18N.CONSTANTS.deletedForm();
                case FORBIDDEN:
                    return I18N.CONSTANTS.forbiddenForm();
            }
            return I18N.CONSTANTS.notFound();
        });
    }
}
