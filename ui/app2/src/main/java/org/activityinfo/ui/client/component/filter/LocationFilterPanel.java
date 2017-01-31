package org.activityinfo.ui.client.component.filter;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.common.base.Function;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetLocations;
import org.activityinfo.legacy.shared.command.SearchLocations;
import org.activityinfo.legacy.shared.command.result.LocationResult;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.filter.FilterToolBar.ApplyFilterEvent;
import org.activityinfo.ui.client.component.filter.FilterToolBar.ApplyFilterHandler;
import org.activityinfo.ui.client.component.filter.FilterToolBar.RemoveFilterEvent;
import org.activityinfo.ui.client.component.filter.FilterToolBar.RemoveFilterHandler;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationFilterPanel extends ContentPanel implements FilterPanel {

    private Dispatcher dispatcher;
    private FilterToolBar filterToolBar;
    private Filter baseFilter = new Filter();
    private Filter value = new Filter();

    private ListStore<LocationDTO> store = new ListStore<>();

    private ComboBox<LocationDTO> filterCombobox;
    private ListView<LocationDTO> listView;
    private ListViewSelectionModel<LocationDTO> listSelectionModel = new ListViewSelectionModel<LocationDTO>();
    private Button removeSelectedItem = new Button(I18N.CONSTANTS.removeSelectedLocations(), IconImageBundle.ICONS.remove());
    

    @Inject
    public LocationFilterPanel(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        initializeComponent();
        createFilterToolBar();
        createList();
    }

    private void initializeComponent() {
        setHeadingText(I18N.CONSTANTS.filterByLocation());
        setIcon(IconImageBundle.ICONS.filter());
        setLayout(new RowLayout(Style.Orientation.VERTICAL));
        setScrollMode(Style.Scroll.NONE);
        setHeadingText(I18N.CONSTANTS.filterByLocation());
        setIcon(IconImageBundle.ICONS.filter());
    }

    private void createFilterToolBar() {
        filterToolBar = new FilterToolBar();
        filterToolBar.add(removeSelectedItem);
        filterToolBar.addApplyFilterHandler(new ApplyFilterHandler() {
            @Override
            public void onApplyFilter(ApplyFilterEvent deleteEvent) {
                applyFilter();
            }
        });
        filterToolBar.addRemoveFilterHandler(new RemoveFilterHandler() {
            @Override
            public void onRemoveFilter(RemoveFilterEvent deleteEvent) {
                clearFilter();
                ValueChangeEvent.fire(LocationFilterPanel.this, value);
            }
        });
        setTopComponent(filterToolBar);
        removeSelectedItem.setEnabled(false);
        removeSelectedItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                for (LocationDTO location : listSelectionModel.getSelection()) {
                    store.remove(location);
                }
            }
        });
        listSelectionModel.addSelectionChangedListener(new SelectionChangedListener<LocationDTO>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<LocationDTO> se) {
                removeSelectedItem.setEnabled(se.getSelectedItem() != null);
            }
        });
    }

    protected void applyFilter() {
        value = new Filter();

        if (isRendered()) {
            List<Integer> selectedIds = getSelectedIds();
            if (selectedIds.size() > 0) {
                value.addRestriction(DimensionType.Location, getSelectedIds());
            }
        }

        ValueChangeEvent.fire(this, value);
        filterToolBar.setApplyFilterEnabled(false);
        filterToolBar.setRemoveFilterEnabled(true);
    }

    private List<Integer> getSelectedIds() {
        List<Integer> list = new ArrayList<Integer>();
        for (LocationDTO model : store.getModels()) {
            list.add(model.getId());
        }
        return list;
    }

    private void createList() {


        ListLoader filterLoader = new BaseListLoader(new LocationProxy());
        ListStore<LocationDTO> filterComboboxStore = new ListStore<>(filterLoader);
        
        filterCombobox = new ComboBox<>();
        filterCombobox.setEmptyText(I18N.CONSTANTS.searchForLocationToAdd());
        filterCombobox.setDisplayField("name");
        filterCombobox.setStore(filterComboboxStore);
        filterCombobox.setUseQueryCache(false); 
        filterCombobox.setTypeAhead(true);
        filterCombobox.setTypeAheadDelay(120);
        filterCombobox.setQueryDelay(100);
        filterCombobox.addSelectionChangedListener(new SelectionChangedListener<LocationDTO>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<LocationDTO> se) {
                if (se.getSelectedItem() != null && !store.contains(se.getSelectedItem())) {
                    store.add(se.getSelectedItem());
                    filterToolBar.setApplyFilterEnabled(true);
                    filterCombobox.setRawValue(""); // reset typed filter
                }
            }
        });
     
        listView = new ListView<>();
        listView.setStore(store);
        listView.setDisplayProperty("name");
        listView.setSelectionModel(listSelectionModel);

        add(filterCombobox, new RowData(1, -1));
        add(listView, new RowData(1, 1));
    }

    protected void clearFilter() {
        store.removeAll();
        value = new Filter();

        filterToolBar.setApplyFilterEnabled(false);
        filterToolBar.setRemoveFilterEnabled(false);
    }

    @Override
    public Filter getValue() {
        return value;
    }

    @Override
    public void setValue(Filter value) {
        setValue(value, false);
    }

    @Override
    public void setValue(final Filter value, final boolean fireEvents) {

        Filter newValue = new Filter();
        newValue.addRestriction(DimensionType.Location, value.getRestrictions(DimensionType.Location));

        if(newValue.equals(this.value)) {
            return;
        }

        this.value = newValue;
        store.removeAll();
        
        if(!this.value.isRestricted(DimensionType.Location)) {
            return;    
        }
        
        applyInternalValue()
                .then(new Function<LocationResult, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@Nullable LocationResult input) {
                        if (fireEvents) {
                            ValueChangeEvent.fire(LocationFilterPanel.this, value);
                        }
                        return null;
                    }
                });
    }

    private Promise<LocationResult> applyInternalValue() {
        Promise<LocationResult> promise = new Promise<>();
        promise.then(new Function<LocationResult, Object>() {
            @Override
            public Object apply(LocationResult input) {
                filterToolBar.setApplyFilterEnabled(false);
                filterToolBar.setRemoveFilterEnabled(value.isRestricted(DimensionType.Location));
                store.removeAll();
                store.add(input.getData());
                return null;
            }
        });
        dispatcher.execute(new GetLocations(new ArrayList<>(value.getRestrictions(DimensionType.Location))), promise);
        return promise;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Filter> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void applyBaseFilter(Filter rawFilter) {
        final Filter filter = new Filter(rawFilter);
        filter.clearRestrictions(DimensionType.Location);

        // avoid fetching a list of ALL locations if no indicators have been selected
        if (!filter.isRestricted(DimensionType.Indicator)) {
            return;
        }
        if (baseFilter == null || !baseFilter.equals(filter)) {
            baseFilter = filter;
        }
    }
    
    private class LocationProxy implements DataProxy<List<LocationDTO>> {

        @Override
        public void load(DataReader<List<LocationDTO>> reader, Object loadConfigObject, 
                         final AsyncCallback<List<LocationDTO>> callback) {

            if (baseFilter == null || baseFilter.isNull()) {
                callback.onSuccess(Collections.<LocationDTO>emptyList());
            }

            PagingLoadConfig loadConfig = (PagingLoadConfig) loadConfigObject;
            String query = loadConfig.get("query");
            
            SearchLocations searchLocations = new SearchLocations()
                    .setName(query)
                    .setIndicatorIds(baseFilter.getRestrictions(DimensionType.Indicator))
                    .setLimit(100);

            dispatcher.execute(searchLocations, new AsyncCallback<LocationResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(LocationResult result) {
                    callback.onSuccess(result.getData());
                }
            });
        }
    }
}
