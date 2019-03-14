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
package org.activityinfo.ui.client.page.common.grid;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.loader.CommandLoadEvent;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.Page;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGridPresenter<ModelT extends ModelData> implements GridPresenter<ModelT>, Page {

    private final EventBus eventBus;
    private final StateProvider stateMgr;
    private final GridView<GridPresenter, ModelT> view;

    protected AbstractGridPresenter(EventBus eventBus, StateProvider stateMgr, GridView view) {
        this.eventBus = eventBus;
        this.stateMgr = stateMgr;
        this.view = view;
    }

    public static boolean equalSortInfo(SortInfo a, SortInfo b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.getSortField().equals(b.getSortField()) && a.getSortDir() == b.getSortDir();
    }

    @Override
    public int getPageSize() {
        return -1;
    }

    protected void initListeners(Store store, Loader loader) {
        if (loader != null) {
            loader.addLoadListener(new LoadListener() {
                @Override
                public void loaderLoad(LoadEvent le) {
                    onLoaded(le);
                }

                @Override
                public void loaderBeforeLoad(LoadEvent le) {
                    if (le instanceof CommandLoadEvent) {
                        onBeforeLoad((CommandLoadEvent) le);
                    }
                    // onBeforeLoad(le);
                }
            });
        }
    }

    @Override
    public void onDirtyFlagChanged(boolean isDirty) {

    }

    @Override
    public void onUIAction(String actionId) {
        if (UIActions.DELETE.equals(actionId)) {
            view.confirmDeleteSelected(new ConfirmCallback() {
                @Override
                public void confirmed() {
                    onDeleteConfirmed(view.getSelection());
                }
            });
        } else if (UIActions.EDIT.equals(actionId)) {
            onEdit(view.getSelection());
        } else if (UIActions.ADD.equals(actionId)) {
            onAdd();
        }
    }

    public int offsetFromPage(int pagenum) {
        return (pagenum - 1) * getPageSize();
    }

    protected void initLoaderDefaults(PagingLoader loader, AbstractPagingGridPageState place, SortInfo defaultSort) {
        Map<String, Object> stateMap = getState();
        if (place.getSortInfo() != null) {
            loader.setSortField(place.getSortInfo().getSortField());
            loader.setSortDir(place.getSortInfo().getSortDir());
        } else if (stateMap.containsKey("sortField")) {
            loader.setSortField((String) stateMap.get("sortField"));
            loader.setSortDir("DESC".equals(stateMap.get("sortDir")) ? Style.SortDir.DESC : Style.SortDir.ASC);
        } else {
            loader.setSortField(defaultSort.getSortField());
            loader.setSortDir(defaultSort.getSortDir());
        }

        loader.setLimit(getPageSize());

        if (place.getPageNum() > 0) {
            loader.setOffset(offsetFromPage(place.getPageNum()));
        } else if (stateMap.containsKey("offset")) {
            loader.setOffset((Integer) stateMap.get("offset"));
        } else {
            loader.setOffset(0);
        }
    }

    protected void onDeleteConfirmed(ModelT model) {

    }

    protected void onAdd() {

    }

    protected void onEdit(ModelT model) {

    }

    protected abstract String getStateId();

    protected Map<String, Object> getState() {

        Map<String, Object> map = stateMgr.getMap(getStateId());
        if (map != null) {
            return map;
        } else {
            return new HashMap<String, Object>();
        }

    }

    protected void saveState(Map<String, Object> stateMap) {
        stateMgr.set(getStateId(), stateMap);
    }

    protected void onBeforeLoad(CommandLoadEvent le) {

    }

    protected void onLoaded(LoadEvent le) {
        Map<String, Object> stateMap = new HashMap<String, Object>();

        Object config = le.getConfig();
        if (config instanceof ListLoadConfig) {
            SortInfo si = ((ListLoadConfig) config).getSortInfo();
            stateMap.put("sortField", si.getSortField());
            stateMap.put("sortDir", si.getSortDir() == Style.SortDir.ASC ? "ASC" : "DESC");
        }
        if (config instanceof PagingLoadConfig) {
            int offset = ((PagingLoadConfig) config).getOffset();
            stateMap.put("offset", offset);
        }

        saveState(stateMap);
    }

    protected void handleGridNavigation(ListLoader loader, AbstractGridPageState gridPlace) {
        boolean reloadRequired = false;

        if (gridPlace.getSortInfo() != null &&
            !equalSortInfo(gridPlace.getSortInfo(), new SortInfo(loader.getSortField(), loader.getSortDir()))) {

            loader.setSortField(gridPlace.getSortInfo().getSortField());
            loader.setSortDir(gridPlace.getSortInfo().getSortDir());
            reloadRequired = true;
        }

        if (gridPlace instanceof AbstractPagingGridPageState) {
            AbstractPagingGridPageState pgPlace = (AbstractPagingGridPageState) gridPlace;

            if (pgPlace.getPageNum() > 0) {
                int offset = offsetFromPage(pgPlace.getPageNum());
                if (offset != ((PagingLoader) loader).getOffset()) {
                    ((PagingLoader) loader).setOffset((pgPlace.getPageNum() - 1) * getPageSize());
                    reloadRequired = true;
                }
            }
        }

        if (reloadRequired) {
            loader.load();
        }
    }

    @Override
    public void requestToNavigateAway(PageState place, NavigationCallback callback) {
        callback.onDecided(true);

    }

    @Override
    public String beforeWindowCloses() {
        return null;
    }

    @Override
    public boolean beforeEdit(Record record, String property) {
        return true;
    }
}
