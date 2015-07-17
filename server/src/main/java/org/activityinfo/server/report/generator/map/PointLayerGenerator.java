package org.activityinfo.server.report.generator.map;

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

import com.google.common.collect.Lists;
import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.model.layers.PointMapLayer;
import org.activityinfo.legacy.shared.reports.util.mapping.Extents;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.Indicator;

import java.util.List;
import java.util.Map;

public abstract class PointLayerGenerator<T extends PointMapLayer> implements LayerGenerator {

    protected T layer;
    protected List<SiteDTO> sites;
    private Map<Integer, Indicator> indicators;

    protected PointLayerGenerator(T layer, Map<Integer, Indicator> indicators) {
        this.layer = layer;
        this.indicators = indicators;
    }

    @Override
    public void query(DispatcherSync dispatcher, Filter effectiveFilter) {
        GetSites query = queryFor(effectiveFilter, layer);
        this.sites = dispatcher.execute(query).getData();
    }

    /**
     * For testing
     *
     * @param sites
     */
    public void setSites(List<SiteDTO> sites) {
        this.sites = sites;
    }

    private GetSites queryFor(Filter effectiveFilter, PointMapLayer layer) {
        Filter layerFilter = new Filter(effectiveFilter, layer.getFilter());
        for (int id : layer.getIndicatorIds()) {
            Indicator indicator = indicators.get(id);
            if (indicator.getAggregation() == IndicatorDTO.AGGREGATE_SITE_COUNT) {
                layerFilter.addRestriction(DimensionType.Activity, indicator.getActivity().getId());
            } else {
                layerFilter.addRestriction(DimensionType.Indicator, indicator.getId());
            }
        }

        layerFilter.addRestriction(DimensionType.Indicator, physicalIndicators(layer));

        GetSites query = new GetSites();
        query.setFilter(layerFilter);
        query.setFetchAttributes(false);
        query.setFetchAdminEntities(true);
        query.setFetchAllIndicators(false);
        query.setFetchIndicators(physicalIndicators(layer));

        return query;
    }

    protected List<Integer> physicalIndicators(PointMapLayer layer) {
        List<Integer> ids = Lists.newArrayList();
        for (int id : layer.getIndicatorIds()) {
            Indicator indicator = indicators.get(id);
            if (indicator.getAggregation() != IndicatorDTO.AGGREGATE_SITE_COUNT) {
                ids.add(id);
            }
        }
        return ids;
    }

    protected boolean hasValue(SiteDTO site, List<Integer> indicatorIds) {

        // if no indicators are specified, we count sites
        if (indicatorIds.size() == 0) {
            return true;
        }

        for (Integer indicatorId : indicatorIds) {
            Double indicatorValue = indicatorValue(site, indicatorId);
            if (indicatorValue != null) {
                return true;
            }
        }
        return false;
    }

    protected Double indicatorValue(SiteDTO site, Integer indicatorId) {
        Indicator indicator = indicators.get(indicatorId);
        if (indicator != null && indicator.getAggregation() == IndicatorDTO.AGGREGATE_SITE_COUNT) {
            return 1d;
        }
        return site.getIndicatorDoubleValue(indicatorId);
    }

    protected Double getValue(SiteDTO site, List<Integer> indicatorIds) {

        // if no indicators are specified, we count sites.
        if (indicatorIds.size() == 0) {
            return 1.0;
        }

        Double value = null;
        for (Integer indicatorId : indicatorIds) {
            Double indicatorValue = indicatorValue(site, indicatorId);
            if (indicatorValue != null) {
                if (value == null) {
                    value = indicatorValue;
                } else {
                    value += indicatorValue;
                }
            }
        }
        return value;
    }

    protected AiLatLng getPoint(SiteDTO site) {
        if (site.hasLatLong()) {
            return new AiLatLng(site.getLatitude(), site.getLongitude());
        } else {
            Extents bounds = getBounds(site);

            if (bounds != null) {
                return bounds.center();
            } else {
                return null;
            }
        }
    }

    protected Extents getBounds(SiteDTO site) {
        // if we don't have a lat/long point, get a centroid from the
        // bounds
        Extents bounds = null;
        for (AdminEntityDTO entity : site.getAdminEntities().values()) {
            if (entity.hasBounds()) {
                if (bounds == null) {
                    bounds = entity.getBounds();
                } else {
                    bounds = bounds.intersect(entity.getBounds());
                }
            }
        }
        return bounds;
    }

}
