package org.activityinfo.core.shared.importing.strategy;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 5/19/14.
 */
public class TargetCollector {

    /**
     * Maps a unique field id to all the FieldPaths within this reference tree
     */
    private final Multimap<ResourceId, FieldPath> fieldPathMap = HashMultimap.create();
    private final List<ImportTarget> targets = Lists.newArrayList();
    private final Map<TargetSiteId, ImportTarget> targetMap = Maps.newHashMap();
    private final FormTree.Node rootField;

    public TargetCollector(FormTree.Node referenceField) {
        rootField = referenceField;
        collectTargetFields(referenceField);
    }

    public void collectTargetFields(FormTree.Node referenceField) {
        for (FormTree.Node child : referenceField.getChildren()) {
            if (child.getType() instanceof TextType) {
                addTargetField(child);
            } else if (child.isReference()) {
                collectTargetFields(child);
            }
        }
    }

    private void addTargetField(FormTree.Node dataField) {
        if (!fieldPathMap.containsKey(dataField.getFieldId())) {
            targets.add(target(dataField));
        }
        fieldPathMap.put(dataField.getFieldId(), dataField.getPath().relativeTo(rootField.getFieldId()));
    }

    public Multimap<ResourceId, FieldPath> getFieldPathMap() {
        return fieldPathMap;
    }

    public List<ImportTarget> getTargets() {
        return targets;
    }

    private ImportTarget target(FormTree.Node dataField) {
        TargetSiteId site = new TargetSiteId(dataField.getFieldId().asString());
        final ImportTarget importTarget = new ImportTarget(rootField.getField(), site, label(dataField), rootField.getDefiningFormClass().getId());
        targetMap.put(site, importTarget);
        return importTarget;
    }

    public ImportTarget getTargetBySiteId(TargetSiteId targetSiteId) {
        return targetMap.get(targetSiteId);
    }

    private String label(FormTree.Node fieldNode) {
        return fieldNode.getDefiningFormClass().getLabel() + " " +
               fieldNode.getField().getLabel();
    }

    public Map<FieldPath, Integer> getPathMap(Map<TargetSiteId, ColumnAccessor> mappings, List<ColumnAccessor> sourceColumns) {
        Map<FieldPath, Integer> referenceValues = new HashMap<>();
        List<FieldImporterColumn> importerColumns = Lists.newArrayList();

        int columnIndex = 0;
        for (Map.Entry<TargetSiteId, ColumnAccessor> entry : mappings.entrySet()) {

            ImportTarget target = getTargetBySiteId(entry.getKey());
            ColumnAccessor source = entry.getValue();

            importerColumns.add(new FieldImporterColumn(target, source));

            sourceColumns.add(entry.getValue());

            ResourceId fieldId = ResourceId.valueOf(entry.getKey().asString());
            for (FieldPath path : getFieldPathMap().get(fieldId)) {
                referenceValues.put(path, columnIndex);
            }

            columnIndex++;
        }
        return referenceValues;
    }

    public List<FieldImporterColumn> fieldImporterColumns(Map<TargetSiteId, ColumnAccessor> mappings) {
        List<FieldImporterColumn> columns = Lists.newArrayList();
        for (Map.Entry<TargetSiteId, ColumnAccessor> entry : mappings.entrySet()) {
            columns.add(new FieldImporterColumn(getTargetBySiteId(entry.getKey()), entry.getValue()));
        }
        return columns;
    }
}
