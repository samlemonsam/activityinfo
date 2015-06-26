package org.activityinfo.ui.client.page.config;

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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.BatchCommand;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.Delete;
import org.activityinfo.legacy.shared.command.UpdateTargetValue;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.ui.client.AppEvents;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.common.grid.AbstractEditorGridPresenter;
import org.activityinfo.ui.client.page.common.grid.TreeGridView;
import org.activityinfo.ui.client.page.common.nav.Link;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetIndicatorPresenter extends AbstractEditorGridPresenter<ModelData> {

    @ImplementedBy(TargetIndicatorView.class)
    public interface View extends TreeGridView<TargetIndicatorPresenter, ModelData> {
        void init(TargetIndicatorPresenter presenter, UserDatabaseDTO db, TreeStore store);

        void expandAll();
    }

    private final EventBus eventBus;
    private final Dispatcher service;
    private final View view;
    private TargetDTO targetDTO;

    private UserDatabaseDTO db;
    private TreeStore<ModelData> treeStore;
    private final Map<Integer, IndicatorDTO> indicators = Maps.newHashMap();

    @Inject
    public TargetIndicatorPresenter(EventBus eventBus,
                                    Dispatcher service,
                                    StateProvider stateMgr,
                                    View view,
                                    UiConstants messages) {
        super(eventBus, service, stateMgr, view);
        this.eventBus = eventBus;
        this.service = service;
        this.view = view;
    }

    public void go(UserDatabaseDTO db) {

        this.db = db;

        treeStore = new TreeStore<ModelData>();

        initListeners(treeStore, null);

        this.view.init(this, db, treeStore);
        this.view.setActionEnabled(UIActions.DELETE, false);
    }

    public void load(Optional<TargetDTO> targetDTO) {
        treeStore.removeAll();

        if (targetDTO.isPresent()) {
            this.targetDTO = targetDTO.get();

            fillStore();
            view.expandAll();
        }
    }

    private void fillStore() {

        Map<String, Link> categories = new HashMap<String, Link>();
        for (ActivityDTO activity : db.getActivities()) {

            if (activity.getCategory() != null) {
                Link actCategoryLink = categories.get(activity.getCategory());

                if (actCategoryLink == null) {

                    actCategoryLink = createCategoryLink(activity, categories);
                    categories.put(activity.getCategory(), actCategoryLink);
                    treeStore.add(actCategoryLink, false);
                }

                treeStore.add(actCategoryLink, activity, false);
                addIndicatorLinks(activity, activity);

            } else {
                treeStore.add(activity, false);
                addIndicatorLinks(activity, activity);
            }

        }
    }

    private void addIndicatorLinks(ActivityDTO activity, ModelData parent) {
        Map<String, Link> indicatorCategories = new HashMap<String, Link>();

        for (IndicatorDTO indicator : activity.getIndicators()) {

            // yuriy : right now we support only quantity indicators in targets, skip other types
            if (indicator.getType() != FieldTypeClass.QUANTITY) {
                continue;
            }

            indicators.put(indicator.getId(), indicator);

            if (indicator.getCategory() != null) {
                Link indCategoryLink = indicatorCategories.get(indicator.getCategory());

                if (indCategoryLink == null) {
                    indCategoryLink = createIndicatorCategoryLink(indicator, indicatorCategories);
                    indicatorCategories.put(indicator.getCategory(), indCategoryLink);
                    treeStore.add(parent, indCategoryLink, false);
                }

                TargetValueDTO targetValueDTO = getTargetValueByIndicatorId(indicator.getId());
                if (null != targetValueDTO) {
                    treeStore.add(indCategoryLink, targetValueDTO, false);
                } else {
                    treeStore.add(indCategoryLink, createTargetValueModel(indicator), false);
                }

            } else {
                TargetValueDTO targetValueDTO = getTargetValueByIndicatorId(indicator.getId());
                if (null != targetValueDTO) {
                    treeStore.add(parent, targetValueDTO, false);
                } else {
                    treeStore.add(parent, createTargetValueModel(indicator), false);
                }
            }
        }

    }

    private TargetValueDTO createTargetValueModel(IndicatorDTO indicator) {
        TargetValueDTO targetValueDTO = new TargetValueDTO();
        targetValueDTO.setTargetId(targetDTO.getId());
        targetValueDTO.setIndicatorId(indicator.getId());
        targetValueDTO.setName(indicator.getName());

        return targetValueDTO;
    }

    private TargetValueDTO getTargetValueByIndicatorId(int indicatorId) {
        List<TargetValueDTO> values = targetDTO.getTargetValues();

        if (values == null) {
            return null;
        }

        for (TargetValueDTO dto : values) {
            if (dto.getIndicatorId() == indicatorId) {
                return dto;
            }
        }

        return null;
    }

    private Link createIndicatorCategoryLink(IndicatorDTO indicatorNode, Map<String, Link> categories) {
        return Link.folderLabelled(indicatorNode.getCategory())
                   .usingKey(categoryKey(indicatorNode, categories))
                   .withIcon(IconImageBundle.ICONS.folder())
                   .build();
    }

    private Link createCategoryLink(ActivityDTO activity, Map<String, Link> categories) {

        return Link.folderLabelled(activity.getCategory())
                   .usingKey(categoryKey(activity, categories))
                   .withIcon(IconImageBundle.ICONS.folder())
                   .build();
    }

    private String categoryKey(ActivityDTO activity, Map<String, Link> categories) {
        return "category" + activity.getDatabaseId() + activity.getCategory() + categories.size();
    }

    private String categoryKey(IndicatorDTO indicatorNode, Map<String, Link> categories) {
        return "category-indicator" + indicatorNode.getCategory() + categories.size();
    }

    @Override
    public Store<ModelData> getStore() {
        return treeStore;
    }

    @Override
    protected void onDeleteConfirmed(final ModelData model) {
        service.execute(new Delete((EntityDTO) model), view.getDeletingMonitor(), new AsyncCallback<VoidResult>() {
            @Override
            public void onFailure(Throwable caught) {
                Log.error("Failed to remove target. ", caught);
            }

            @Override
            public void onSuccess(VoidResult result) {
                treeStore.remove(model);
                eventBus.fireEvent(AppEvents.SCHEMA_CHANGED);
            }
        });
    }

    @Override
    protected String getStateId() {
        return "target" + db.getId();
    }

    @Override
    protected Command createSaveCommand() {
        lastChanges.clear();

        BatchCommand batch = new BatchCommand();

        for (ModelData model : treeStore.getRootItems()) {
            prepareBatch(batch, model);
        }
        return batch;
    }

    final List<TargetValueDTO> lastChanges = Lists.newArrayList();

    protected void prepareBatch(BatchCommand batch, ModelData model) {
        if (model instanceof EntityDTO) {
            Record record = treeStore.getRecord(model);
            if (record.isDirty()) {
                Map<String, Double> changes = changes(record);

                TargetValueDTO targetValue = new TargetValueDTO();
                targetValue.setValue(changes.get("value"));
                targetValue.setIndicatorId((Integer) model.get("indicatorId"));
                targetValue.setTargetId((Integer) model.get("targetId"));
                targetValue.setName(indicators.get(targetValue.getIndicatorId()).getName());

                UpdateTargetValue cmd = new UpdateTargetValue(targetValue.getTargetId(),
                        targetValue.getIndicatorId(), changes);

                batch.add(cmd);
                lastChanges.add(targetValue);
            }
        }

        for (ModelData child : treeStore.getChildren(model)) {
            prepareBatch(batch, child);
        }
    }

    private Map<String, Double> changes(Record record) {
        Map<String, Object> changedProperties = this.getChangedProperties(record);
        Map<String, Double> changes = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : changedProperties.entrySet()) {
            if (entry.getValue() instanceof Double) {
                changes.put(entry.getKey(), (Double) entry.getValue());
            } else if (entry.getValue() instanceof String) {
                changes.put(entry.getKey(), Double.valueOf((String) entry.getValue()));
            }
        }
        return changes;
    }

    @Override
    public void onSelectionChanged(ModelData selectedItem) {
        view.setActionEnabled(UIActions.DELETE, this.db.isDesignAllowed() && selectedItem instanceof EntityDTO);
    }

    @Override
    public Object getWidget() {
        return view;
    }

    @Override
    protected void onSaved() {
        treeStore.commitChanges();

        // update target dto
        for (TargetValueDTO targetValue : Lists.newArrayList(targetDTO.getTargetValues())) {
            for (TargetValueDTO change : lastChanges) {
                if (targetValue.getIndicatorId() == change.getIndicatorId() &&
                        targetValue.getTargetId() == change.getTargetId()) {
                    targetValue.setValue(change.getValue());
                } else {
                    targetDTO.getTargetValues().add(change);
                }

            }
        }
    }

    @Override
    public PageId getPageId() {
        return null;
    }

    @Override
    public boolean navigate(PageState place) {
        return false;
    }

    @Override
    public void shutdown() {
    }
}
