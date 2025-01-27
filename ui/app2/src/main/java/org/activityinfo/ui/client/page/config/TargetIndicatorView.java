/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.page.config;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.EditorTreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.type.IndicatorNumberFormat;
import org.activityinfo.ui.client.page.common.grid.AbstractEditorTreeGridView;
import org.activityinfo.ui.client.page.common.grid.ImprovedCellTreeGridSelectionModel;
import org.activityinfo.ui.client.page.common.nav.Link;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Muhammad Abid
 */

public class TargetIndicatorView extends AbstractEditorTreeGridView<ModelData, TargetIndicatorPresenter> implements
        TargetIndicatorPresenter.View {

    private final Dispatcher service;

    private EditorTreeGrid<ModelData> tree;
    private TargetIndicatorPresenter presenter;
    private UserDatabaseDTO db;

    @Inject
    public TargetIndicatorView(Dispatcher service) {
        this.service = service;
    }

    @Override
    public void init(TargetIndicatorPresenter presenter, UserDatabaseDTO db, TreeStore store) {

        this.db = db;
        this.presenter = presenter;
        super.init(presenter, store);

        setBorders(false);
        setHeaderVisible(false);
        setFrame(false);
        setLayout(new FitLayout());
    }

    @Override
    protected Grid<ModelData> createGridAndAddToContainer(Store store) {

        final TreeStore treeStore = (TreeStore) store;

        tree = new EditorTreeGrid<ModelData>(treeStore, createColumnModel());
        tree.setAutoExpandColumn("name");
        tree.setSelectionModel(new ImprovedCellTreeGridSelectionModel<ModelData>());
        tree.setClicksToEdit(EditorGrid.ClicksToEdit.ONE);
        tree.setLoadMask(true);
        tree.setStateId("TargetValueGrid" + db.getId());
        tree.setStateful(true);

        tree.setIconProvider(new ModelIconProvider<ModelData>() {
            @Override
            public AbstractImagePrototype getIcon(ModelData model) {

                if (model instanceof IsActivityDTO) {
                    return IconImageBundle.ICONS.activity();
                } else if (model instanceof TargetValueDTO) {
                    return IconImageBundle.ICONS.indicator();
                } else if (model instanceof Link) {
                    return IconImageBundle.ICONS.folder();
                } else {
                    return null;
                }

            }
        });

        addBeforeEditListener();
        addAfterEditListener();

        add(tree, new BorderLayoutData(Style.LayoutRegion.CENTER));

        return tree;
    }

    private void addBeforeEditListener() {
        tree.addListener(Events.BeforeEdit, new Listener<GridEvent>() {

            @Override
            public void handleEvent(GridEvent be) {
                if (!(be.getModel() instanceof TargetValueDTO)) {
                    be.setCancelled(true);
                    return;
                }

                setValidatorForCellBeforeEdit((TargetValueDTO) be.getModel(), be.getColIndex());

            }

        });
    }

    private void setValidatorForCellBeforeEdit(TargetValueDTO targetValueDTO, int column) {
        TextField field = new TextField<String>();
        field.setAllowBlank(true);

        IndicatorDTO indicatorById = presenter.getIndicatorById(targetValueDTO.getIndicatorId());
        FieldTypeClass type = indicatorById.getType();
        if (type == FieldTypeClass.QUANTITY) {
            field = new NumberField();
            ((NumberField) field).setFormat(IndicatorNumberFormat.INSTANCE);
            field.setAllowBlank(true);
        }

        tree.getColumnModel().getColumn(column).setEditor(new CellEditor(field));
    }

    private List<ActivityDTO> getActivitiesFromStore() {
        List<ActivityDTO> activities = Lists.newArrayList();
        for (int i = 0; i < tree.getStore().getCount(); i++) {
            if (tree.getStore().getAt(i) instanceof ActivityDTO) {
                activities.add((ActivityDTO) tree.getStore().getAt(i));
            }
        }
        return activities;
    }

    private void addAfterEditListener() {

        tree.addListener(Events.AfterEdit, new Listener<GridEvent>() {

            @Override
            public void handleEvent(GridEvent be) {
                if (be.getModel() instanceof TargetValueDTO) {

                    if (be.getRecord().getChanges().size() > 0) {
                        presenter.onSave();
                    }
                }
            }

        });
    }

    @Override
    public void expandAll() {
        tree.expandAll();
    }

    @Override
    protected void initToolBar() {

    }

    private ColumnModel createColumnModel() {

        List<ColumnConfig> columns = new ArrayList<>();

        ColumnConfig nameColumn = new ColumnConfig("name", I18N.CONSTANTS.indicator(), 250);
        nameColumn.setRenderer(new TreeGridCellRenderer());
        columns.add(nameColumn);

        TextField<String> valueField = new TextField<String>();
        valueField.setAllowBlank(true);

        ColumnConfig valueColumn = new ColumnConfig("value", I18N.CONSTANTS.targetValue(), 150);
        valueColumn.setEditor(new CellEditor(valueField));
        valueColumn.setRenderer(new TargetValueCellRenderer());
        columns.add(valueColumn);

        return new ColumnModel(columns);
    }

    private class TargetValueCellRenderer implements GridCellRenderer<ModelData> {

        TargetValueCellRenderer() {
        }

        @Override
        public SafeHtml render(ModelData model,
                               String property,
                               ColumnData config,
                               int rowIndex,
                               int colIndex,
                               ListStore store,
                               Grid grid) {

            if(model instanceof TargetValueDTO) {

                Double targetValue = ((TargetValueDTO) model).getValue();

                if (targetValue == null) {
                    config.style = "color:gray;font-style:italic;";
                    return SafeHtmlUtils.fromSafeConstant(I18N.CONSTANTS.noTarget());

                } else {
                    return SafeHtmlUtils.fromTrustedString(targetValue.toString());
                }

            } else {
                return SafeHtmlUtils.EMPTY_SAFE_HTML;
            }
        }
    }
}