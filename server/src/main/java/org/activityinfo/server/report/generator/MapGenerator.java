package org.activityinfo.server.report.generator;

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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetBaseMaps;
import org.activityinfo.legacy.shared.command.result.BaseMapResult;
import org.activityinfo.legacy.shared.model.BaseMap;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.TileBaseMap;
import org.activityinfo.legacy.shared.reports.content.GoogleBaseMap;
import org.activityinfo.legacy.shared.reports.content.MapContent;
import org.activityinfo.legacy.shared.reports.content.PredefinedBaseMaps;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.legacy.shared.reports.model.layers.*;
import org.activityinfo.legacy.shared.reports.util.mapping.Extents;
import org.activityinfo.legacy.shared.reports.util.mapping.TileMath;
import org.activityinfo.model.form.FormFieldType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.dao.IndicatorDAO;
import org.activityinfo.server.database.hibernate.entity.Country;
import org.activityinfo.server.database.hibernate.entity.Indicator;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.report.generator.map.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Bertram
 */
public class MapGenerator extends ListGenerator<MapReportElement> {

    private final IndicatorDAO indicatorDAO;

    private static final Logger LOGGER = Logger.getLogger(MapGenerator.class.getName());

    @Inject
    public MapGenerator(DispatcherSync dispatcher, IndicatorDAO indicatorDAO) {
        super(dispatcher);
        this.indicatorDAO = indicatorDAO;
    }

    @Override
    public void generate(User user, MapReportElement element, Filter inheritedFilter, DateRange dateRange) {

        Filter filter = GeneratorUtils.resolveElementFilter(element, dateRange);
        Filter effectiveFilter = inheritedFilter == null ? filter : new Filter(inheritedFilter, filter);

        MapContent content = new MapContent();
        content.setFilterDescriptions(generateFilterDescriptions(filter, Collections.<DimensionType>emptySet(), user));

        Map<Integer, Indicator> indicators = queryIndicators(element);

        // Set up layer generators
        List<LayerGenerator> layerGenerators = new ArrayList<LayerGenerator>();
        for (MapLayer layer : element.getLayers()) {
            if (layer.isVisible()) {
                LayerGenerator layerGtor = createGenerator(layer, indicators);
                layerGtor.query(getDispatcher(), effectiveFilter);
                layerGenerators.add(layerGtor);
            }
        }

        // FIRST PASS: calculate extents and margins
        int width = element.getWidth();
        int height = element.getHeight();
        AiLatLng center;
        int zoom;

        Extents extents = Extents.emptyExtents();
        Margins margins = new Margins(0);
        for (LayerGenerator layerGtor : layerGenerators) {
            extents.grow(layerGtor.calculateExtents());
            margins.grow(layerGtor.calculateMargins());
        }
        content.setExtents(extents);

        if (element.getCenter() == null) {

            // Now we're ready to calculate the zoom level
            // and the projection
            zoom = TileMath.zoomLevelForExtents(extents, width, height);
            center = extents.center();

        } else {
            center = element.getCenter();
            zoom = element.getZoomLevel();
        }

        content.setCenter(center);


        // Retrieve the basemap and clamp zoom level
        BaseMap baseMap = findBaseMap(element, indicators.values());

        if (zoom < baseMap.getMinZoom()) {
            zoom = baseMap.getMinZoom();
        }
        if (zoom > baseMap.getMaxZoom()) {
            zoom = baseMap.getMaxZoom();
        }
        if (zoom > element.getMaximumZoomLevel()) {
            zoom = element.getMaximumZoomLevel();
        }

        TiledMap map = new TiledMap(width, height, center, zoom);
        content.setBaseMap(baseMap);
        content.setZoomLevel(zoom);

        // Generate the actual content
        for (LayerGenerator layerGtor : layerGenerators) {
            layerGtor.generate(map, content);
        }

        content.setIndicators(toDTOs(indicators.values()));
        element.setContent(content);

    }

    private LayerGenerator createGenerator(MapLayer layer, Map<Integer, Indicator> indicators) {
        if (layer instanceof BubbleMapLayer) {
            return new BubbleLayerGenerator((BubbleMapLayer) layer, indicators);
        } else if (layer instanceof IconMapLayer) {
            return new IconLayerGenerator((IconMapLayer) layer, indicators);
        } else if (layer instanceof PiechartMapLayer) {
            return new PiechartLayerGenerator((PiechartMapLayer) layer, indicators);
        } else if (layer instanceof PolygonMapLayer) {
            return new PolygonLayerGenerator((PolygonMapLayer) layer);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Map<Integer, Indicator> queryIndicators(MapReportElement element) {

        // Get relevant indicators for the map markers
        Set<Integer> indicatorIds = new HashSet<Integer>();
        for (MapLayer maplayer : element.getLayers()) {
            indicatorIds.addAll(maplayer.getIndicatorIds());
        }

        Map<Integer, Indicator> indicators = Maps.newHashMap();
        for (Integer indicatorId : indicatorIds) {
            indicators.put(indicatorId, indicatorDAO.findById(indicatorId));
        }
        return indicators;
    }

    private Set<IndicatorDTO> toDTOs(Collection<Indicator> indicators) {
        Set<IndicatorDTO> indicatorDTOs = new HashSet<IndicatorDTO>();
        for (Indicator indicator : indicators) {
            IndicatorDTO indicatorDTO = new IndicatorDTO();
            indicatorDTO.setId(indicator.getId());
            indicatorDTO.setName(indicator.getName());
            indicatorDTO.setType(FormFieldType.valueOf(indicator.getType()));
            indicatorDTO.setExpression(indicator.getExpression());
            indicatorDTO.setSkipExpression(indicator.getSkipExpression());

            indicatorDTOs.add(indicatorDTO);
        }
        return indicatorDTOs;
    }

    private BaseMap findBaseMap(MapReportElement element, Collection<Indicator> indicators) {
        BaseMap baseMap = null;
        String baseMapId = element.getBaseMapId();
        if (element.getBaseMapId() == null || element.getBaseMapId().equals(MapReportElement.AUTO_BASEMAP)) {
            baseMapId = defaultBaseMap(indicators);
        }
        if (PredefinedBaseMaps.isPredefinedMap(baseMapId)) {
            baseMap = PredefinedBaseMaps.forId(baseMapId);
        } else {
            baseMap = getBaseMap(baseMapId);
        }
        return baseMap;
    }

    private String defaultBaseMap(Collection<Indicator> indicators) {
        Set<Country> countries = queryCountries(indicators);
        if (countries.size() == 1) {
            Country country = countries.iterator().next();
            if ("CD".equals(country.getCodeISO())) {
                return "admin";
            }
        }
        return GoogleBaseMap.ROADMAP.getId();
    }

    private Set<Country> queryCountries(Collection<Indicator> indicators) {
        Set<Country> country = Sets.newHashSet();
        for (Indicator indicator : indicators) {
            country.add(indicator.getActivity().getDatabase().getCountry());
        }
        return country;
    }

    private BaseMap getBaseMap(String baseMapId) {
        BaseMapResult maps = dispatcher.execute(new GetBaseMaps());
        for (TileBaseMap map : maps.getBaseMaps()) {
            if (map.getId().equals(baseMapId)) {
                return map;
            }
        }
        LOGGER.log(Level.SEVERE, "Could not find base map id=" + baseMapId);

        return TileBaseMap.createNullMap(baseMapId);
    }
}
