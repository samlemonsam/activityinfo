package org.activityinfo.core.shared.importing.match;
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
import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.core.shared.importing.model.MapExistingAction;
import org.activityinfo.core.shared.importing.source.SourceColumn;
import org.activityinfo.core.shared.importing.strategy.ImportTarget;
import org.activityinfo.model.expr.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author yuriyz on 5/7/14.
 */
public class ColumnMappingGuesser {

    private final ImportModel importModel;
    private final List<ImportTarget> importTargets;

    public ColumnMappingGuesser(ImportModel importModel, List<ImportTarget> importTargets) {
        this.importModel = importModel;
        this.importTargets = importTargets;
    }

    public void guess() {
        final Map<SourceColumn, ImportTarget> mapping = getMapping();

        // remove from guessed map entries which are already set (we don't want to override existing bindings)
        for (SourceColumn sourceColumn : importModel.getColumnActions().keySet()) {
            mapping.remove(sourceColumn);
        }

        // set binding
        for (Map.Entry<SourceColumn, ImportTarget> entry : mapping.entrySet()) {
            importModel.setColumnBinding(new MapExistingAction(entry.getValue()), entry.getKey());
        }
    }

    public Map<SourceColumn, ImportTarget> getMapping() {
        // lower distance between maps
        Map<SourceColumn, TreeMap<Integer, ImportTarget>> distanceWithinTargetMaps = Maps.newHashMap();
        for (SourceColumn sourceColumn : importModel.getSource().getColumns()) {
            final String sourceColumnHeader = sourceColumn.getHeader();
            final TreeMap<Integer, ImportTarget> distanceMap = getDistanceMap(sourceColumnHeader);
            if (!distanceMap.isEmpty()) {
                final Map.Entry<Integer, ImportTarget> lowerDistanceEntry = distanceMap.entrySet().iterator().next();

                // if number of transformation operations are higher then label length then ignore such mapping
                final Integer transformationOperations = lowerDistanceEntry.getKey();
//                if (transformationOperations < sourceColumnHeader.length() && transformationOperations < lowerDistanceEntry.getValue().getLabel().length()) {
                TreeMap<Integer, ImportTarget> valueMap = distanceWithinTargetMaps.get(sourceColumn);
                if (valueMap == null) {
                    valueMap = Maps.newTreeMap();
                    distanceWithinTargetMaps.put(sourceColumn, valueMap);
                }
                valueMap.put(transformationOperations, lowerDistanceEntry.getValue());
//                }
            }
        }

        // now re-iterate for target (different source columns may get the same target column as best match (lower distance))
        Map<ImportTarget, TreeMap<Integer, SourceColumn>> targetToSource = Maps.newHashMap();
        for (Map.Entry<SourceColumn, TreeMap<Integer, ImportTarget>> entry : distanceWithinTargetMaps.entrySet()) {
            final TreeMap<Integer, ImportTarget> value = entry.getValue();
            if (!value.isEmpty()) {
                final Map.Entry<Integer, ImportTarget> bestEntry = value.entrySet().iterator().next(); // entry with lowest distance
                TreeMap<Integer, SourceColumn> distanceForSourceMap = targetToSource.get(bestEntry.getValue());
                if (distanceForSourceMap == null) {
                    distanceForSourceMap = Maps.newTreeMap();
                    targetToSource.put(bestEntry.getValue(), distanceForSourceMap);
                }
                distanceForSourceMap.put(bestEntry.getKey(), entry.getKey());

            }
        }

        // finally build mapping
        Map<SourceColumn, ImportTarget> mapping = Maps.newHashMap();
        for (Map.Entry<ImportTarget, TreeMap<Integer, SourceColumn>> entry : targetToSource.entrySet()) {
            final TreeMap<Integer, SourceColumn> map = entry.getValue();
            if (!map.isEmpty()) {
                mapping.put(map.entrySet().iterator().next().getValue(), entry.getKey());
            }
        }
        return mapping;
    }

    public TreeMap<Integer, ImportTarget> getDistanceMap(String sourceLabel) {
        final TreeMap<Integer, ImportTarget> distanceMap = Maps.newTreeMap();
        for (ImportTarget target : importTargets) {
            final String targetLabel = target.getLabel();
            final int distance = StringUtil.getLevenshteinDistance(sourceLabel, targetLabel);
            distanceMap.put(distance, target);
        }
        return distanceMap;
    }
}
