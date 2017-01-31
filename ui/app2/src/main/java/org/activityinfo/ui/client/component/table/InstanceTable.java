package org.activityinfo.ui.client.component.table;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.RangeChangeEvent;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.expr.GroupExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.component.table.action.*;
import org.activityinfo.ui.client.component.table.filter.FilterCellAction;
import org.activityinfo.ui.client.component.table.filter.FilterHeader;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.style.table.CellTableResources;
import org.activityinfo.ui.client.widget.CellTable;
import org.activityinfo.ui.client.widget.loading.TableLoadingIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable component to display Instances in a table
 */
public class InstanceTable implements IsWidget {

//    private static final Logger LOGGER = Logger.getLogger(InstanceTable.class.getName());

    /**
     * The default column width, in {@code em}
     */
    public static final int COLUMN_WIDTH = 10;

    private final ResourceLocator resourceLocator;
    private final StateProvider stateProvider;

    private final CellTable<RowView> table;
    private final MultiSelectionModel<RowView> selectionModel = new MultiSelectionModel<>(new RowViewKeyProvider());
    private final List<TableHeaderAction> headerActions;
    private final InstanceTableView tableView;
    private final InstanceTableDataLoader dataLoader;
    private final ColumnStatePersister columnStatePersister;
    private final TableLoadingIndicator loadingIndicator = new TableLoadingIndicator()
            .setHideOnSuccess(true);

    private FormClass rootFormClass;
    private List<FieldColumn> columns = Lists.newArrayList();

    public InstanceTable(InstanceTableView tableView) {
        this.tableView = tableView;
        this.resourceLocator = tableView.getResourceLocator();
        this.columnStatePersister = new ColumnStatePersister(tableView.getStateProvider());
        this.stateProvider = tableView.getStateProvider();

        CellTableResources.INSTANCE.cellTableStyle().ensureInjected();

        final TableHeaderActionBrowserEventHandler headerActionEventHandler = new TableHeaderActionBrowserEventHandler(this);
        table = new CellTable<RowView>(Integer.MAX_VALUE, CellTableResources.INSTANCE) {
            @Override
            protected void onBrowserEvent2(Event event) {
                super.onBrowserEvent2(event);
                headerActionEventHandler.onBrowserEvent(event);
            }
        };
        table.setSkipRowHoverCheck(true);
        table.setSkipRowHoverFloatElementCheck(true);
        table.setSkipRowHoverStyleUpdate(true);
        table.setHeaderBuilder(new InstanceTableHeaderBuilder(this));

        // Set the table to fixed width: we will provide explicit
        // column widths
        table.setWidth("100%", true);
        table.setSelectionModel(selectionModel);
        table.addRangeChangeHandler(new RangeChangeEvent.Handler() {
            @Override
            public void onRangeChange(RangeChangeEvent event) {
                table.redrawHeaders();
            }
        });

        dataLoader = new InstanceTableDataLoader(this);

        loadingIndicator.getRetryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dataLoader.loadMore();
            }
        });

        headerActions = createHeaderActions();

    }

    private List<TableHeaderAction> createHeaderActions() {
        final List<TableHeaderAction> actions = new ArrayList<>();
        actions.add(new NewHeaderAction(this));
        actions.add(new DeleteHeaderAction(this));
        actions.add(new EditHeaderAction(this));
        actions.add(new PrintFormAction(this));
        actions.add(new ImportHeaderAction(this));
        actions.add(new ExportHeaderAction(this));
        actions.add(new ChooseColumnsHeaderAction(this));
        return actions;
    }

    public void setColumns(List<FieldColumn> columns) {
        removeAllColumns();
        this.dataLoader.reset();
        this.columns = Lists.newArrayList(columns);

        for (FieldColumn column : columns) {
            final FilterCellAction filterAction = new FilterCellAction(this, column);
            table.addColumn(column, new FilterHeader(column, filterAction));
            dataLoader.getFields().addAll(column.get().getFieldPaths());
        }

        reload();
        table.saveColumnWidthInformation();
    }

    private void removeAllColumns() {
        while (table.getColumnCount() > 0) {
            table.removeColumn(0);
        }
    }

    public ExprNode getFilter() {
        List<ExprNode> arguments = Lists.newArrayList();
        for (FieldColumn column : columns) {
            if (column.get().getFilter() != null) {
                arguments.add(new GroupExpr(column.get().getFilter()));
            }
        }

        if(arguments.isEmpty()) {
            return null;
        } else {
            return Exprs.allTrue(arguments);
        }
    }

    public InstanceTableDataLoader getDataLoader() {
        return dataLoader;
    }

    public void reload() {
        dataLoader.reload();
    }

    public MultiSelectionModel<RowView> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public Widget asWidget() {
        return table;
    }

    public List<FieldColumn> getColumns() {
        return columns;
    }

    public CellTable<RowView> getTable() {
        return table;
    }

    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }

    public void setRootFormClass(FormClass rootFormClass) {
        this.rootFormClass = rootFormClass;
    }

    public FormClass getRootFormClass() {
        return rootFormClass;
    }

    public List<TableHeaderAction> getHeaderActions() {
        return headerActions;
    }

    public InstanceTableView getTableView() {
        return tableView;
    }

    public TableLoadingIndicator getLoadingIndicator() {
        return loadingIndicator;
    }

    public StateProvider getStateProvider() {
        return stateProvider;
    }
    
    public void loadMore() {
        dataLoader.loadMore();
    }

    public ColumnStatePersister getColumnStatePersister() {
        return columnStatePersister;
    }
}
