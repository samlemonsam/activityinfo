package org.activityinfo.ui.client.component.table;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.widget.AlertPanel;
import org.activityinfo.ui.client.widget.loading.TableLoadingIndicator;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Displays the this classes' FormInstances in a table format
 */
public class InstanceTableView implements IsWidget, RequiresResize {

    private static final int DEFAULT_MAX_COLUMN_COUNT = 5;
    private static final Logger LOGGER = Logger.getLogger(InstanceTableView.class.getName());

    private final ResourceLocator resourceLocator;
    private final StateProvider stateProvider;

    private final HTMLPanel panel;
    private List<FieldColumn> columns;
    private List<FieldColumn> selectedColumns;

    @UiField
    DivElement emRuler;
    @UiField
    AlertPanel columnAlert;
    @UiField(provided = true)
    InstanceTable table;
    @UiField
    AlertPanel errorMessages;
    @UiField(provided = true)
    TableLoadingIndicator loadingIndicator;

    interface InstanceTableViewUiBinder extends UiBinder<HTMLPanel, InstanceTableView> {
    }

    private static InstanceTableViewUiBinder ourUiBinder = GWT.create(InstanceTableViewUiBinder.class);

    public InstanceTableView(ResourceLocator resourceLocator, StateProvider stateProvider) {

        InstanceTableStyle.INSTANCE.ensureInjected();

        this.resourceLocator = resourceLocator;
        this.stateProvider = stateProvider;

        this.table = new InstanceTable(this);
        this.loadingIndicator = table.getLoadingIndicator();
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    public void setColumns(final List<FieldColumn> columns) {
        this.columns = columns;
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                calculateSelectedColumns();
            }
        });
    }

    public void setSelectedColumns(final List<FieldColumn> selectedColumns) {
        this.selectedColumns = selectedColumns;
        table.setColumns(selectedColumns);
        final int allColumns = columns.size();
        final int visibleColumns = selectedColumns.size();
        if (visibleColumns < allColumns) {
            columnAlert.showMessages(I18N.MESSAGES.notAllColumnsAreShown(visibleColumns, allColumns, I18N.CONSTANTS.chooseColumns()));
        } else {
            columnAlert.setVisible(false);
        }
    }

    private void calculateSelectedColumns() {
        Set<String> persistedColumnNames = table.getColumnStatePersister().getColumnNames();
        if (!persistedColumnNames.isEmpty()) {
            List<FieldColumn> toSelect = Lists.newArrayList();
            for (FieldColumn column : columns) {
                if (!Strings.isNullOrEmpty(column.get().getHeader()) && persistedColumnNames.contains(column.get().getHeader())) {
                    toSelect.add(column);
                }
            }

            if (!toSelect.isEmpty()) {
                setSelectedColumns(toSelect);
                return;
            }
        }

        if (columns.size() <= getMaxNumberOfColumns()) {
            setSelectedColumns(Lists.newArrayList(columns));
        } else {
            setSelectedColumns(Lists.newArrayList(columns.subList(0, getMaxNumberOfColumns())));
        }
    }

    public int getMaxNumberOfColumns() {
        double emSizeInPixels = ((double) emRuler.getOffsetWidth()) / 100d;

        LOGGER.log(Level.FINE, "emSizeInPixels = " + emSizeInPixels);

        double columnWidthInPixels = InstanceTable.COLUMN_WIDTH * emSizeInPixels;

        int columnLimit = (int) Math.floor(panel.getElement().getClientWidth() / columnWidthInPixels);
        LOGGER.log(Level.FINE, "columnLimit = " + columnLimit);
        if (columnLimit <= 0) { // fallback : yuriyz: check calculations above
            columnLimit = DEFAULT_MAX_COLUMN_COUNT;
        }
        return columnLimit;
    }

    public InstanceTable getTable() {
        return table;
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    public void onResize() {
    }

    public List<FieldColumn> getColumns() {
        if (columns == null) {
            columns = Lists.newArrayList();
        }
        return columns;
    }

    public List<FieldColumn> getSelectedColumns() {
        if (selectedColumns == null) {
            selectedColumns = Lists.newArrayList();
        }
        return selectedColumns;
    }

    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }

    public void setRootFormClass(FormClass rootFormClass) {
        table.setRootFormClass(rootFormClass);
    }

    public FormClass getRootFormClass() {
        return table.getRootFormClass();
    }

    public StateProvider getStateProvider() {
        return stateProvider;    
    }
}