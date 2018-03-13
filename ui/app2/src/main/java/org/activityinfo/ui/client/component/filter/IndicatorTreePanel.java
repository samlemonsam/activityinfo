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
package org.activityinfo.ui.client.component.filter;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.CheckCascade;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.callback.SuccessCallback;
import org.activityinfo.ui.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * UI Component that allows the user to select a list of Indicators
 *
 * @author Alex Bertram
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IndicatorTreePanel extends ContentPanel {

    private final Dispatcher dispatcher;
    private ResourceLocator resourceLocator;

    private final TreeStore<ModelData> store;
    private final TreePanel<ModelData> tree;
    private ToolBar toolBar;
    private StoreFilterField filter;
    private boolean multipleSelection;

    interface Templates extends SafeHtmlTemplates {

        @Template("<b>{0}</b>")
        SafeHtml headerNode(String name);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    /**
     * Keep our own copy of our selection state that is independent of the
     * loading process
     */
    private Set<Integer> selection = Sets.newHashSet();

    public IndicatorTreePanel(Dispatcher dispatcher, ResourceLocator resourceLocator) {
        this.dispatcher = dispatcher;
        this.resourceLocator = resourceLocator;

        this.setHeadingText(I18N.CONSTANTS.indicators());
        this.setIcon(IconImageBundle.ICONS.indicator());
        this.setLayout(new FitLayout());
        this.setScrollMode(Style.Scroll.NONE);

        store = new TreeStore<>(new Loader());

        setStoreKeyProvider();

        tree = new TreePanel<>(store);

        tree.setCheckNodes(TreePanel.CheckNodes.BOTH);
        tree.setCheckStyle(CheckCascade.CHILDREN);

        tree.getStyle().setNodeCloseIcon(null);
        tree.getStyle().setNodeOpenIcon(null);

        tree.setLabelProvider(new NodeLabelProvider());

        tree.setCheckable(true);
        tree.expandAll();
        tree.setStateId("indicatorPanel");
        tree.setStateful(true);
        tree.setAutoSelect(true);
        tree.addListener(Events.BrowserEvent, (TreePanelEvent<ModelData> event) -> {
            if (event.getEventTypeInt() == Event.ONKEYPRESS) {
                if (!toolBar.isVisible()) {
                    toolBar.setVisible(true);
                }
                filter.focus();
            }
        });

        add(tree);
        createFilterBar();

        tree.getStore().getLoader().addLoadListener(new LoadListener() {

            @Override
            public void loaderLoad(LoadEvent le) {
                Log.info("Loaded " + le);
            }
        });

        tree.getStore().addStoreListener(new StoreListener<ModelData>() {

            @Override
            public void storeDataChanged(StoreEvent<ModelData> se) {
                // apply our internal state to the newly loaded list
                applySelection();
            }
        });

        addCheckChangedListener(event -> {
            if (event.getItem() instanceof IndicatorDTO) {
                IndicatorDTO indicator = (IndicatorDTO) event.getItem();
                if (event.isChecked()) {
                    selection.add(indicator.getId());
                } else {
                    selection.remove(indicator.getId());
                }
            } else if (event.isChecked()) {
                tree.getStore().getLoader().loadChildren(event.getItem());
            }
        });
    }

    private void setStoreKeyProvider() {
        store.setKeyProvider(model -> {
            if (model instanceof ProvidesKey) {
                return ((ProvidesKey) model).getKey();
            } else if (model == null) {
                throw new IllegalStateException(
                        "Did not expect model to be null: assigning keys in IndicatorTreePanel");
            }
            throw new IllegalStateException("Unknown type: expected activity, userdb, indicator or indicatorgroup");
        });
    }

    private void createFilterBar() {
        toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(I18N.CONSTANTS.search()));
        filter = new FilterField();
        filter.addListener(Events.Blur, event -> {
            if (filter.getRawValue() == null || filter.getRawValue().length() == 0) {
                toolBar.setVisible(false);
            }
        });
        toolBar.add(filter);
        toolBar.setVisible(false);
        filter.bind(store);
        setTopComponent(toolBar);
    }

    private final class NodeLabelProvider implements ModelStringProvider<ModelData> {
        @Override
        public SafeHtml getStringValue(ModelData model, String property) {
            String name = model.get("name");
            if (model instanceof IndicatorDTO) {
                return SafeHtmlUtils.fromString(((IndicatorDTO) model).getName());
            } else {
                if (name == null) {
                    name = "noname";
                }
                return TEMPLATES.headerNode(name);
            }
        }
    }

    private class Proxy implements DataProxy<List<ModelData>> {
        private SchemaDTO schema;

        @Override
        public void load(DataReader<List<ModelData>> listDataReader,
                         Object parent,
                         final AsyncCallback<List<ModelData>> callback) {

            if (parent == null) {
                dispatcher.execute(new GetSchema(),
                        new MaskingAsyncMonitor(IndicatorTreePanel.this, I18N.CONSTANTS.loading()),
                        new SuccessCallback<SchemaDTO>() {

                            @Override
                            public void onSuccess(SchemaDTO result) {
                                schema = result;
                                callback.onSuccess(new ArrayList<ModelData>(schema.getDatabases()));
                            }
                        });
            } else if (parent instanceof UserDatabaseDTO) {

                callback.onSuccess(new ArrayList<ModelData>(createDatabaseChildren((UserDatabaseDTO) parent)));

            } else if (parent instanceof FolderDTO) {

                callback.onSuccess(new ArrayList<ModelData>(((FolderDTO) parent).getActivities()));

            } else if (parent instanceof ActivityDTO) {

                loadActivityChildren((ActivityDTO) parent).then(callback);

            } else if (parent instanceof IndicatorGroup) {

                callback.onSuccess(createIndicatorList((IndicatorGroup) parent));
            }
        }

        private List<ModelData> createDatabaseChildren(UserDatabaseDTO databaseDTO) {

            List<ModelData> children = new ArrayList<>();

            Set<FolderDTO> categories = new HashSet<>();

            for (ActivityDTO activityDTO : databaseDTO.getActivities()) {
                if (activityDTO.hasCategory()) {
                    FolderDTO folder = new FolderDTO(databaseDTO.getId(), activityDTO.getCategory());
                    categories.add(folder);
                    if (!children.contains(folder)) {
                        children.add(folder);
                    }
                } else {
                    children.add(activityDTO);
                }
            }

            // fill category with activities
            for (FolderDTO category : categories) {
                for (ActivityDTO activityDTO : databaseDTO.getActivities()) {
                    if (category.getName().equals(activityDTO.getCategory())) {
                        category.addActivity(activityDTO);
                    }
                }
            }

            return children;
        }

        private Promise<List<ModelData>> loadActivityChildren(ActivityDTO activity) {

            if(activity.getClassicView()) {
                return loadClassicActivityChildren(activity);
            } else {
                return loadFields(activity.getFormId());
            }
        }


        private Promise<List<ModelData>> loadClassicActivityChildren(ActivityDTO activity) {
            return dispatcher.execute(new GetActivityForm(activity.getId())).then(form -> {
                List<ModelData> children = new ArrayList<>();
                for (IndicatorGroup group : form.groupIndicators()) {
                    if (group.getName() == null) {
                        for (IndicatorDTO indicator : group.getIndicators()) {
                            if (indicator.getType() == FieldTypeClass.QUANTITY) {
                                children.add(indicator);
                            }
                        }
                    } else {
                        children.add(group);
                    }
                }
                return children;
            });
        }


        private List<ModelData> createIndicatorList(IndicatorGroup group) {
            ArrayList<ModelData> list = new ArrayList<>();
            for (IndicatorDTO indicator : group.getIndicators()) {
                if (indicator.getType() == FieldTypeClass.QUANTITY) {
                    list.add(indicator);
                }
            }

            return list;
        }


        private Promise<List<ModelData>> loadFields(ResourceId formId) {

            return resourceLocator.getFormClass(formId).then(formClass -> {
                List<ModelData> children = new ArrayList<>();
                for (FormField formField : formClass.getFields()) {
                    if(formField.getType() instanceof QuantityType ||
                            formField.getType() instanceof CalculatedFieldType) {
                        IndicatorDTO indicator = new IndicatorDTO();
                        indicator.setId(CuidAdapter.getLegacyIdFromCuid(formField.getId()));
                        indicator.setName(formField.getLabel());
                        children.add(indicator);
                    }
                }
                return children;
            });
        }
    }

    /**
     * @return the list of selected indicators
     */
    public List<IndicatorDTO> getSelection() {
        List<IndicatorDTO> list = new ArrayList<>();
        for (ModelData model : tree.getCheckedSelection()) {
            if (model instanceof IndicatorDTO) {
                list.add((IndicatorDTO) model);
            }
        }
        return list;
    }

    public void select(int indicatorId, boolean select) {

        if (select) {
            if (!multipleSelection) {
                selection.clear();
            }
            selection.add(indicatorId);
        } else {
            selection.remove(indicatorId);
        }

        // apply directly if the indicators are loaded
        for (ModelData model : tree.getStore().getAllItems()) {
            if (model instanceof IndicatorDTO && ((IndicatorDTO) model).getId() == indicatorId) {
                setChecked((IndicatorDTO) model, select);
            }
        }
    }

    public void setSelection(Iterable<Integer> indicatorIds) {
        selection = Sets.newHashSet(indicatorIds);
        applySelection();
    }

    public void addCheckChangedListener(Listener<TreePanelEvent> listener) {
        tree.addListener(Events.CheckChange, listener);
    }

    /**
     * @return the list of the ids of selected indicators
     */
    public List<Integer> getSelectedIds() {
        return Lists.newArrayList(selection);
    }

    private class Loader extends BaseTreeLoader<ModelData> {
        public Loader() {
            super(new Proxy());
        }

        @Override
        public boolean hasChildren(ModelData parent) {
            return !(parent instanceof IndicatorDTO);
        }
    }

    private static class FilterField extends StoreFilterField<ModelData> {
        @Override
        protected boolean doSelect(Store store, ModelData parent, ModelData record, String property, String filter) {

            String[] keywords = filter.toLowerCase().split("\\s+");
            String name = record.get(EntityDTO.NAME_PROPERTY);
            String loweredName = name.toLowerCase();
            for (String keyword : keywords) {
                if (!loweredName.contains(keyword)) {
                    return false;
                }
            }
            return true;
        }
    }

    public void clearSelection() {
        for (IndicatorDTO indicator : getSelection()) {
            tree.getSelectionModel().deselect(indicator);
            tree.setChecked(indicator, false);
        }
    }

    public void setChecked(IndicatorDTO indicator, boolean b) {
        tree.setChecked(indicator, b);
    }

    public void setLeafCheckableOnly() {
        tree.setCheckNodes(TreePanel.CheckNodes.LEAF);
    }

    private void applySelection() {
        for (ModelData model : tree.getStore().getAllItems()) {
            if (model instanceof IndicatorDTO) {
                IndicatorDTO indicator = (IndicatorDTO) model;
                boolean selected = selection.contains(indicator.getId());
                setChecked(indicator, selected);
            }
        }
    }
}
