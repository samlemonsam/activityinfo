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
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
import com.google.inject.Provider;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.ProjectionKeyProvider;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.CriteriaUnion;
import org.activityinfo.core.shared.criteria.CriteriaVisitor;
import org.activityinfo.core.shared.criteria.FieldCriteria;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.widget.DataGrid;
import org.activityinfo.ui.client.widget.DisplayWidget;
import org.activityinfo.ui.client.widget.LoadingPanel;
import org.activityinfo.ui.client.widget.TextBox;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

/**
 * @author yuriyz on 4/3/14.
 */
public class FilterContentExistingItems extends Composite implements FilterContent {

    public static final String FILTER_GRID_HEIGHT = "250px";
    public static final int CHECKBOX_COLUMN_WIDTH = 20;

    /**
     * Search box is shown if number of items is more or equals to SEARCH_BOX_PRESENCE_ITEM_COUNT.
     * Otherwise it's removed from filter panel.
     */
    private static final int SEARCH_BOX_PRESENCE_ITEM_COUNT = 7;

    interface FilterContentStringUiBinder extends UiBinder<HTMLPanel, FilterContentExistingItems> {
    }

    private static FilterContentStringUiBinder uiBinder = GWT.create(FilterContentStringUiBinder.class);

    private final ListDataProvider<Projection> tableDataProvider = new ListDataProvider<>();
    private final MultiSelectionModel<Projection> selectionModel = new MultiSelectionModel<>(new ProjectionKeyProvider());

    private final FieldColumn column;
    private final DataGrid<Projection> filterGrid;
    private List<Projection> allItems;

    @UiField
    TextBox textBox;
    @UiField
    LoadingPanel<QueryResult<Projection>> loadingPanel;
    @UiField
    HTMLPanel textBoxContainer;

    public FilterContentExistingItems(final FieldColumn column, final InstanceTable table) {

        initWidget(uiBinder.createAndBindUi(this));

        this.column = column;

        textBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                filterData();
            }
        });

        final Column<Projection, Boolean> checkColumn = new Column<Projection, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(Projection object) {
                return selectionModel.isSelected(object);
            }
        };

        filterGrid = new DataGrid<>(1000, FilterDataGridResources.INSTANCE);
        filterGrid.setSelectionModel(selectionModel, DefaultSelectionEventManager
                .<Projection>createCheckboxManager());
        filterGrid.addColumn(checkColumn);
        filterGrid.addColumn(column);
        filterGrid.setColumnWidth(checkColumn, CHECKBOX_COLUMN_WIDTH, Style.Unit.PX);
        filterGrid.setHeight(FILTER_GRID_HEIGHT);
        filterGrid.setAutoHeaderRefreshDisabled(true);
        filterGrid.setAutoFooterRefreshDisabled(true);

        tableDataProvider.addDataDisplay(filterGrid);

        loadingPanel.setDisplayWidget(new DisplayWidget<QueryResult<Projection>>() {
            @Override
            public Promise<Void> show(QueryResult<Projection> values) {
                allItems = extractItems(values.getProjections());
                if (allItems.size() < SEARCH_BOX_PRESENCE_ITEM_COUNT) {
                    textBoxContainer.remove(textBox);
                }

                filterData();
                initByCriteriaVisit();
                return Promise.done();
            }

            @Override
            public Widget asWidget() {
                return filterGrid;
            }
        });
        loadingPanel.show(new Provider<Promise<QueryResult<Projection>>>() {
            @Override
            public Promise<QueryResult<Projection>> get() {
                InstanceQuery query = table.getDataLoader().createInstanceQuery(0, 10000)
                        .setUniqueValueForGivenColumn(true);
                return table.getResourceLocator().queryProjection(query);
            }
        });
    }

    private void initByCriteriaVisit() {
        final Criteria criteria = column.getCriteria();
        if (criteria != null) {
            final CriteriaVisitor initializationVisitor = new CriteriaVisitor() {
                @Override
                public void visitFieldCriteria(FieldCriteria fieldCriteria) {
                    for (Projection projection : allItems) {
                        final Object valueAsObject = column.getFieldValue(projection);
                        if (Objects.equals(valueAsObject, fieldCriteria.getValue())) {
                            selectionModel.setSelected(projection, true);
                        }
                    }
                }

                @Override
                public void visitUnion(CriteriaUnion criteriaUnion) {
                    for (Criteria criteria : criteriaUnion.getElements()) {
                        criteria.accept(this);
                    }
                }
            };
            criteria.accept(initializationVisitor);
        }
    }

    private List<Projection> extractItems(List<Projection> projections) {
        final SortedMap<String, Projection> labelToProjectionMap = Maps.newTreeMap();
        for (Projection projection : projections) {
            final String value = column.getValue(projection).replace(String.valueOf((char) 160), " ").trim();
            if (!Strings.isNullOrEmpty(value) && !labelToProjectionMap.containsKey(value)) {
                labelToProjectionMap.put(value, projection);
            }
        }
        return Lists.newArrayList(labelToProjectionMap.values());
    }

    private void filterData() {
        final String stringFilter = textBox.getValue();
        final List<Projection> toShow = Lists.newArrayList();
        for (Projection projection : allItems) {
            final String value = column.getValue(projection);
            if (Strings.isNullOrEmpty(stringFilter) || value.contains(stringFilter)) {
                toShow.add(projection);
            }
        }
        tableDataProvider.setList(toShow);
    }

    @Override
    public Criteria getCriteria() {
        final Set<Projection> selectedSet = selectionModel.getSelectedSet();
        final List<Criteria> criteriaList = Lists.newArrayList();
        for (Projection projection : selectedSet) {
            criteriaList.add(new FieldCriteria(column.getNode().getPath(), column.getFieldValue(projection)));
        }
        return new CriteriaUnion(criteriaList);
    }

    private void selectAll(boolean selectState) {
        for (Projection projection : tableDataProvider.getList()) {
            selectionModel.setSelected(projection, selectState);
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
}
