package org.activityinfo.ui.client.component.importDialog.model.strategy;
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
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 5/19/14.
 */
public class HierarchyClassTargetBuilder {

    private final FormTree.Node rootField;
    private final TargetCollector targetCollector;

    public HierarchyClassTargetBuilder(FormTree.Node referenceField) {
        rootField = referenceField;
        targetCollector= new TargetCollector(referenceField);
    }

    public List<ImportTarget> getTargets() {
        return targetCollector.getTargets();
    }

    public HierarchyClassImporter newImporter(Map<TargetSiteId, ColumnAccessor> mappings) {
        List<ColumnAccessor> sourceColumns = Lists.newArrayList();
        Map<FieldPath, Integer> referenceValues = targetCollector.getPathMap(mappings, sourceColumns);
        List<FieldImporterColumn> fieldImporterColumns = targetCollector.fieldImporterColumns(mappings);
        return new HierarchyClassImporter(rootField, sourceColumns, referenceValues, fieldImporterColumns);
    }
}
