package org.activityinfo.ui.client.component.form.field.map;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.reports.content.MapboxLayers;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.ui.client.page.entry.form.resources.SiteFormResources;
import org.activityinfo.ui.client.style.table.CellTableResources;
import org.activityinfo.ui.client.widget.TextBox;
import org.discotools.gwt.leaflet.client.LeafletResourceInjector;
import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.crs.epsg.EPSG3857;
import org.discotools.gwt.leaflet.client.events.MouseEvent;
import org.discotools.gwt.leaflet.client.events.handler.EventHandler;
import org.discotools.gwt.leaflet.client.events.handler.EventHandlerManager;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.others.LayerGroup;
import org.discotools.gwt.leaflet.client.layers.raster.TileLayer;
import org.discotools.gwt.leaflet.client.map.Map;
import org.discotools.gwt.leaflet.client.map.MapOptions;
import org.discotools.gwt.leaflet.client.marker.Marker;
import org.discotools.gwt.leaflet.client.types.*;

import java.util.List;
import java.util.Set;

/**
 * Created by yuriyz on 9/14/2016.
 */
public class MapPanel implements IsWidget {

    public static final int ZOOM_LEVEL = 6;

    interface MapPanelUiBinder extends UiBinder<DockLayoutPanel, MapPanel> {
    }

    private static MapPanelUiBinder ourUiBinder = GWT.create(MapPanelUiBinder.class);

    private final DockLayoutPanel root;
    private Map map;
    private final ListDataProvider<MapItem> tableDataProvider = new ListDataProvider<>();
    private final SingleSelectionModel<MapItem> selectionModel = new SingleSelectionModel<>(new ProvidesKey<MapItem>() {
        @Override
        public Object getKey(MapItem item) {
            return item.getId();
        }
    });
    private final Set<MapItem> allItems;

    @UiField
    DivElement mapContainer;
    @UiField(provided = true)
    CellTable<MapItem> table;
    @UiField
    TextBox filterBox;

    public MapPanel(Set<MapItem> items) {
        allItems = items;
        table = createTable();
        root = ourUiBinder.createAndBindUi(this);

        LeafletResourceInjector.ensureInjected();

        root.getElement().getStyle().setWidth(ChooseReferenceMapDialog.width() - 40, Style.Unit.PX);
        root.getElement().getStyle().setHeight(ChooseReferenceMapDialog.height() - 150, Style.Unit.PX);

        root.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        init();
                    }
                });
            }
        });

        filterBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                setTableItems();
            }
        });
    }

    private void setTableItems() {
        if (Strings.isNullOrEmpty(filterBox.getValue())) {
            tableDataProvider.setList(Lists.newArrayList(allItems));
            return;
        }

        List<MapItem> filteredItems = Lists.newArrayList();
        for (MapItem item : allItems) {
            if (Strings.nullToEmpty(item.getLabel()).contains(filterBox.getValue())) {
                filteredItems.add(item);
            }
        }
        tableDataProvider.setList(filteredItems);
    }

    private CellTable createTable() {
        table = new CellTable<>(Integer.MAX_VALUE, CellTableResources.INSTANCE);
        table.setSkipRowHoverCheck(true);
        table.setSkipRowHoverFloatElementCheck(true);
        table.setSkipRowHoverStyleUpdate(false);
        table.setSelectionModel(selectionModel);

        // Set the table to fixed width: we will provide explicit
        // column widths
        table.setWidth("100%", true);
        table.addColumn(new TextColumn<MapItem>() {
            @Override
            public String getValue(MapItem item) {
                return item.getLabel();
            }
        }, I18N.CONSTANTS.name());

        tableDataProvider.addDataDisplay(table);
        tableDataProvider.setList(Lists.newArrayList(allItems));

        addTableSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                MapPanel.this.onSelectionChange(selectionModel.getSelectedObject());
            }
        });

        return table;
    }

    private void onSelectionChange(MapItem selectedItem) {
        if (selectedItem.hasLatLng()) {
            selectOnMap(selectedItem);
        } else {
            Log.error("MapItem does not have geo point set, id: " + selectedItem);
        }
    }

    private void selectOnMap(MapItem mapItem) {
        //map.fitBounds(new LatLngBounds().extend(latLng));
        map.setView(mapItem.getLatLng(), ZOOM_LEVEL, true);
        //map.panTo(mapItem.getLatLng());
    }

    public void addTableSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
        selectionModel.addSelectionChangeHandler(handler);
    }

    public MapItem getSelectedItem() {
        return selectionModel.getSelectedObject();
    }

    public Optional<ReferenceValue> getValue() {
        MapItem selectedItem = getSelectedItem();
        if (selectedItem != null) {
            return Optional.of(new ReferenceValue(ResourceId.valueOf(selectedItem.getId())));
        }
        return Optional.absent();
    }

    private void init() {
        MapOptions mapOptions = new MapOptions();
        mapOptions.setZoom(6);
        mapOptions.setProperty("crs", new EPSG3857());

        TileLayer baseLayer = new TileLayer(MapboxLayers.MAPBOX_STREETS, new Options());

        LayerGroup markerLayer = new LayerGroup(new ILayer[0]);

        this.map = new Map(mapContainer, mapOptions);
        this.map.addLayer(baseLayer);
        this.map.addLayer(markerLayer);

        LatLngBounds bounds = new LatLngBounds();

        boolean selectedFirst = false;

        for (MapItem mapItem : allItems) {
            if (mapItem.hasLatLng()) {

                Marker marker = createMarker(mapItem.getLatLng(), mapItem.getLabel());
                markerLayer.addLayer(marker);

                bounds.extend(mapItem.getLatLng());
                bindClickEvent(mapItem, marker);

                if (!selectedFirst) {
                    selectOnMap(mapItem);
                    selectedFirst = true;
                }
            }
        }

        map.fitBounds(bounds);

    }

    private void bindClickEvent(final MapItem mapItem, Marker marker) {
        EventHandlerManager.addEventHandler(marker,
                org.discotools.gwt.leaflet.client.events.handler.EventHandler.Events.click,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        selectionModel.setSelected(mapItem, true);
                    }
                });
    }

    private Marker createMarker(LatLng latLng, String label) {
        DivIcon icon = createIcon(label);

        Options markerOptions = new Options();
        markerOptions.setProperty("icon", icon);

        return new Marker(latLng, markerOptions);
    }

    private DivIcon createIcon(String label) {
        ImageResource markerImage = SiteFormResources.INSTANCE.blankMarker();

        DivIconOptions iconOptions = new DivIconOptions();
        iconOptions.setClassName(SiteFormResources.INSTANCE.style().locationMarker());
        iconOptions.setIconSize(new Point(markerImage.getWidth(), markerImage.getHeight()));
        iconOptions.setIconAnchor(new Point(markerImage.getWidth() / 2, markerImage.getHeight()));
        iconOptions.setHtml(label);

        return new DivIcon(iconOptions);
    }

    @Override
    public Widget asWidget() {
        return root;
    }
}
