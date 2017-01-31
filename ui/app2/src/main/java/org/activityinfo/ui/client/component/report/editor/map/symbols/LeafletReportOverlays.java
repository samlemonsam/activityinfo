package org.activityinfo.ui.client.component.report.editor.map.symbols;

import com.google.common.base.Objects;
import com.google.gwt.http.client.*;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.model.BaseMap;
import org.activityinfo.legacy.shared.model.TileBaseMap;
import org.activityinfo.legacy.shared.reports.content.AdminOverlay;
import org.activityinfo.legacy.shared.reports.content.MapContent;
import org.activityinfo.legacy.shared.reports.content.MapMarker;
import org.activityinfo.legacy.shared.reports.content.MapboxLayers;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.ui.client.util.LeafletUtil;
import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.events.Event;
import org.discotools.gwt.leaflet.client.events.handler.EventHandler;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.others.GeoJSON;
import org.discotools.gwt.leaflet.client.layers.others.GeoJSONOptions;
import org.discotools.gwt.leaflet.client.layers.others.LayerGroup;
import org.discotools.gwt.leaflet.client.layers.raster.TileLayer;

import java.util.List;

public class LeafletReportOverlays {

    private final org.discotools.gwt.leaflet.client.map.Map mapWidget;
    // private final Map<Overlay, MapMarker> markers = new HashMap<Overlay, MapMarker>();

    private TileLayer baseLayer;
    private LayerGroup adminLayer;
    private LayerGroup markerLayer;
    private TileBaseMap currentBaseMap;

    public LeafletReportOverlays(org.discotools.gwt.leaflet.client.map.Map mapWidget) {
        super();
        this.mapWidget = mapWidget;

        this.adminLayer = new LayerGroup(new ILayer[0]);
        this.mapWidget.addLayer(adminLayer);

        this.markerLayer = new LayerGroup(new ILayer[0]);
        this.mapWidget.addLayer(markerLayer);
    }

    public void clear() {
        markerLayer.clearLayers();
        adminLayer.clearLayers();
    }

    public void setBaseMap(BaseMap baseMap) {
        TileBaseMap tileBaseMap = MapboxLayers.toTileBaseMap(baseMap);
        if (!Objects.equal(currentBaseMap, tileBaseMap)) {
            if (baseLayer != null) {
                mapWidget.removeLayer(baseLayer);
            }

            Options options = new Options();
            options.setProperty("minZoom", tileBaseMap.getMinZoom());
            options.setProperty("maxZoom", tileBaseMap.getMaxZoom());
            baseLayer = new TileLayer(tileBaseMap.getTileUrlPattern(), options);
            mapWidget.addLayer(baseLayer, true);

            if (mapWidget.getZoom() > tileBaseMap.getMaxZoom()) {
                mapWidget.setZoom(tileBaseMap.getMaxZoom());
            }
            if (mapWidget.getZoom() < tileBaseMap.getMinZoom()) {
                mapWidget.setZoom(tileBaseMap.getMinZoom());
            }
        }
    }

    public Extents addMarkers(List<MapMarker> markers, EventHandler<Event> markerEventHandler) {
        Extents extents = Extents.emptyExtents();
        for (MapMarker marker : markers) {
            markerLayer.addLayer(LeafletMarkerFactory.create(marker, markerEventHandler));
            extents.grow(marker.getLat(), marker.getLng());
        }
        return extents;
    }

    public void setView(MapContent content) {
        mapWidget.setView(LeafletUtil.to(content.getCenter()), content.getZoomLevel(), false);
    }

    public void syncWith(MapReportElement element) {
        clear();
        addMarkers(element.getContent().getMarkers(), null);
        setBaseMap(element.getContent().getBaseMap());
        for (AdminOverlay overlay : element.getContent().getAdminOverlays()) {
            addAdminLayer(overlay);
        }
        setView(element.getContent());
    }

    public void addAdminLayer(final AdminOverlay adminOverlay) {

        RequestBuilder request = new RequestBuilder(RequestBuilder.GET,
                "/resources/adminLevel/" + adminOverlay.getAdminLevelId() + "/entities/features");
        request.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                try {
                    GeoJSONOptions options = new GeoJSONOptions(new AdminChloroplethFeatures(adminOverlay));
                    GeoJSON layer = new GeoJSON(response.getText(), options);
                    adminLayer.addLayer(layer);
                } catch (Exception e) {
                    Log.error("Error adding features to map", e);
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Log.error("Error requesting features", exception);
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            Log.error("Failed to send request", e);
        }
    }
}