package org.activityinfo.ui.client.component.table.filter;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.component.table.RowView;
import org.activityinfo.ui.client.component.table.RowViewKeyProvider;
import org.activityinfo.ui.client.widget.DataGrid;
import org.activityinfo.ui.client.widget.DisplayWidget;
import org.activityinfo.ui.client.widget.LoadingPanel;
import org.activityinfo.ui.client.widget.TextBox;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yuriyz on 4/3/14.
 */
public class FilterContentExistingItems extends Composite implements FilterContent {

    private static final Logger LOGGER = Logger.getLogger(FilterContentExistingItems.class.getName());

    public static final int FILTER_GRID_HEIGHT = 250;
    public static final int CHECKBOX_COLUMN_WIDTH = 20;

    /**
     * Search box is shown if number of items is more or equals to SEARCH_BOX_PRESENCE_ITEM_COUNT.
     * Otherwise it's removed from filter panel.
     */
    private static final int SEARCH_BOX_PRESENCE_ITEM_COUNT = 7;

    interface FilterContentStringUiBinder extends UiBinder<HTMLPanel, FilterContentExistingItems> {
    }

    private static FilterContentStringUiBinder uiBinder = GWT.create(FilterContentStringUiBinder.class);

    private final ListDataProvider<RowView> tableDataProvider = new ListDataProvider<>();
    private final MultiSelectionModel<RowView> selectionModel = new MultiSelectionModel<>(new RowViewKeyProvider());

    private final FieldColumn column;
    private final DataGrid<RowView> filterGrid;

    private List<RowView> allItems;
    private ValueChangeHandler changeHandler;

    @UiField
    TextBox textBox;
    @UiField
    LoadingPanel<List<RowView>> loadingPanel;
    @UiField
    HTMLPanel textBoxContainer;
    @UiField
    HTMLPanel messageSpanContainer;
    @UiField
    SpanElement messageSpan;

    public FilterContentExistingItems(final FieldColumn column, final InstanceTable table, final FilterPanel popup) {

        initWidget(uiBinder.createAndBindUi(this));

        this.column = column;

        textBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                filterData();
            }
        });

        final Column<RowView, Boolean> checkColumn = new Column<RowView, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(RowView object) {
                return selectionModel.isSelected(object);
            }
        };

        filterGrid = new DataGrid<>(1000, FilterDataGridResources.INSTANCE);
        filterGrid.setSelectionModel(selectionModel, DefaultSelectionEventManager
                .<RowView>createCheckboxManager());
        filterGrid.addColumn(checkColumn);
        filterGrid.addColumn(column);
        filterGrid.setColumnWidth(checkColumn, CHECKBOX_COLUMN_WIDTH, Style.Unit.PX);
        filterGrid.setHeight(FILTER_GRID_HEIGHT + "px");
        filterGrid.setAutoHeaderRefreshDisabled(true);
        filterGrid.setAutoFooterRefreshDisabled(true);

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (changeHandler != null) {
                    changeHandler.onValueChange(null);
                }
            }
        });

        tableDataProvider.addDataDisplay(filterGrid);

        loadingPanel.setDisplayWidget(new DisplayWidget<List<RowView>>() {
            @Override
            public Promise<Void> show(List<RowView> values) {
                allItems = extractItems(values);
                if (allItems.size() < SEARCH_BOX_PRESENCE_ITEM_COUNT) {
                    textBoxContainer.remove(textBox);
                }

                filterData();
                initByCriteriaVisit();

                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {
                        popup.forcePopupToBeVisibleLater();
                        return false;
                    }
                }, 1000);

                return Promise.done();
            }

            @Override
            public Widget asWidget() {
                return filterGrid;
            }
        });
        loadingPanel.show(new Provider<Promise<List<RowView>>>() {
            @Override
            public Promise<List<RowView>> get() {
                final QueryModel model = table.getDataLoader().newQueryModel();
                model.selectField(column.getFieldPaths().get(0));

                final Promise<List<RowView>> result = new Promise<>();
                table.getResourceLocator().queryTable(model).subscribe(new Observer<ColumnSet>() {
                    @Override
                    public void onChange(Observable<ColumnSet> observable) {
                        if (!observable.isLoading()) {
                            ColumnSet columnSet = observable.get();
                            if (columnSet != null) {

                                Set<String> values = Sets.newHashSet();
                                List<RowView> rows = Lists.newArrayList();

                                // only unique values
                                for (int row = 0; row < columnSet.getNumRows(); row++) {
                                    RowView rowView = new RowView(row, columnSet.getColumns());
                                    Object value = rowView.getValue(column.getNode().getFieldId().asString());
                                    String valueStr = value != null ? value.toString() : "";
                                    if (!Strings.isNullOrEmpty(valueStr) && !values.contains(valueStr)) {
                                        values.add(valueStr);
                                        rows.add(rowView);
                                    }
                                }
                                result.resolve(rows);
                            }
                        }
                    }
                });
                return result;
            }
        });
    }

    private void initByCriteriaVisit() {
        // todo
        final ExprNode node = column.getFilter();
//        if (criteria != null) {
//            final CriteriaVisitor initializationVisitor = new CriteriaVisitor() {
//                @Override
//                public void visitFieldCriteria(FieldCriteria fieldCriteria) {
//                    for (Projection projection : allItems) {
//                        final Object valueAsObject = column.getFieldValue(projection);
//                        if (Objects.equals(valueAsObject, fieldCriteria.getValue())) {
//                            selectionModel.setSelected(projection, true);
//                        }
//                    }
//                }
//
//                @Override
//                public void visitUnion(CriteriaUnion criteriaUnion) {
//                    for (Criteria criteria : criteriaUnion.getElements()) {
//                        criteria.accept(this);
//                    }
//                }
//            };
//            criteria.accept(initializationVisitor);
//        }
    }

    private List<RowView> extractItems(List<RowView> rowViews) {
        final SortedMap<String, RowView> labelToRowMap = Maps.newTreeMap();
        for (RowView rowView : rowViews) {
            final String value = column.getValue(rowView).replace(String.valueOf((char) 160), " ").trim();
            if (!Strings.isNullOrEmpty(value) && !labelToRowMap.containsKey(value)) {
                labelToRowMap.put(value, rowView);
            }
        }
        return Lists.newArrayList(labelToRowMap.values());
    }

    private void filterData() {
        final String stringFilter = textBox.getValue();
        final List<RowView> toShow = Lists.newArrayList();
        for (RowView rowView : allItems) {
            final String value = column.getValue(rowView);
            if (Strings.isNullOrEmpty(stringFilter) || value.toUpperCase().contains(stringFilter.toUpperCase())) {
                toShow.add(rowView);
            }
        }
        tableDataProvider.setList(toShow);
    }

    private void selectAll(boolean selectState) {
        for (RowView rowView : tableDataProvider.getList()) {
            selectionModel.setSelected(rowView, selectState);
        }
    }

    @UiHandler("selectAllButton")
    public void onSelectAll(ClickEvent event) {
        selectAll(true);
    }

    @UiHandler("deselectAllButton")
    public void onDeselectAll(ClickEvent event) {
        selectAll(false);
    }

    @Override
    public void clear() {
        selectAll(false);
    }

    @Override
    public boolean isValid() {
        boolean isValid = !selectionModel.getSelectedSet().isEmpty();

        messageSpan.setInnerHTML(SafeHtmlUtils.fromString(I18N.CONSTANTS.pleaseSelectAtLeastOneItem()).asString());

        messageSpanContainer.setVisible(!isValid);
        return isValid;
    }

    @Override
    public void setChangeHandler(ValueChangeHandler handler) {
        this.changeHandler = handler;
    }

    @Override
    public ExprNode getFilter() {
        if (isValid()) {
            try {
                String expr = "";
                List<RowView> set = Lists.newArrayList(selectionModel.getSelectedSet());
                int size = set.size();
                for (int i = 0; i < size; i++) {
                    RowView row = set.get(i);
                    String id = column.getNode().getFieldId().asString();
                    boolean isNumber = column.getNode().getType() instanceof QuantityType;

                    Object value = row.getValue(id);
                    if (value != null && !Strings.isNullOrEmpty(value.toString())) {

                        expr += id + "==";
                        if (!isNumber) {
                            expr += "'";
                        }
                        expr += value.toString();
                        if (!isNumber) {
                            expr += "'";
                        }

                        if ((i + 1) != size) { // if not last
                            expr += " || ";
                        }
                    }
                }
                if (size > 1) {
                    expr = "(" + expr + ")";
                }
                return ExprParser.parse(expr);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return null;
    }
}
