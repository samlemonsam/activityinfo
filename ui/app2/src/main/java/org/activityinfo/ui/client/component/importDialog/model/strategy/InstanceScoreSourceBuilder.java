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
package org.activityinfo.ui.client.component.importDialog.model.strategy;

import com.google.common.collect.Lists;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 5/20/14.
 */
public class InstanceScoreSourceBuilder {

    private ResourceId formId;
    private final Map<FieldPath, Integer> referenceFields;
    private final List<ColumnAccessor> sourceColumns;
    private final boolean[] roots;

    public InstanceScoreSourceBuilder(ResourceId formId, Map<FieldPath, Integer> referenceFields, List<ColumnAccessor> sourceColumns) {
        this.formId = formId;
        this.referenceFields = referenceFields;
        this.sourceColumns = sourceColumns;

        this.roots = new boolean[sourceColumns.size()];
        for (Map.Entry<FieldPath, Integer> entry : referenceFields.entrySet()) {
            if(entry.getKey().isRoot() && entry.getKey().toString().startsWith(formId.asString())) {
                roots[entry.getValue()] = true;
            }
        }

    }

    public InstanceScoreSource build(ColumnSet columnSet) {
        final List<ResourceId> recordIdList = Lists.newArrayList();
        final List<String[]> referenceValueList = Lists.newArrayList();

        ColumnView id = columnSet.getColumnView("_id");
        
        for (int i = 0; i < columnSet.getNumRows(); i++) {
            recordIdList.add(ResourceId.valueOf(id.getString(i)));
            referenceValueList.add(SingleClassImporter.toArray(columnSet, i, referenceFields, sourceColumns.size()));
        }
        return new InstanceScoreSource(sourceColumns, roots, recordIdList, referenceValueList);
    }
}
