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
package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Set of keys from the source collection that are used to lookup the ids of a reference field
 * of the target form.
 */
public class SourceKeySet {

    
    /**
     * List of source fields that constitute the lookup key
     */
    private final List<FieldProfile> sourceFields = new ArrayList<>();
    private final List<FieldProfile> targetFields = new ArrayList<>();

    /**
     * List of unique source lookup keys
     */
    private final List<SourceLookupKey> distinctKeys;

    public SourceKeySet(KeyFieldPairSet fieldMatching) {

        // Which columns do we use to lookup the reference field? 
        // For example: if we are looking up the id of a territory, then 
        // we might use the territory name, territory code, and province name 
        // fields present in a shapefile or other imported set

        for (FieldProfile targetField : fieldMatching.getTarget().getFields()) {
            Optional<FieldProfile> sourceField = fieldMatching.targetToSource(targetField);
            if(sourceField.isPresent() && sourceField.get().isText()) {
                sourceFields.add(sourceField.get());
                targetFields.add(targetField);
            }
        }

        // Together, the values of these columns form a lookup key.
        // Find all DISTINCT tuples of this key
        Set<SourceLookupKey> keys = new HashSet<>();
        for(int i=0;i<fieldMatching.getSource().getRowCount(); ++i) {
            keys.add(SourceLookupKey.build(sourceFields, i));
        }
        
        this.distinctKeys = Lists.newArrayList(keys);
    }

    /**
     * For a given index of a row within the source table, find the index of the distinct
     * lookup key.
     */
    public int getKeyIndexOfSourceRow(int sourceRowIndex) {
        SourceLookupKey key = SourceLookupKey.build(sourceFields, sourceRowIndex);
        return distinctKeys.indexOf(key);
    }
    
    public List<FieldProfile> getSourceFields() {
        return sourceFields;
    }

    public List<FieldProfile> getTargetFields() {
        return targetFields;
    }

    public List<SourceLookupKey> distinct() {
        return distinctKeys;
    }

    public int size() {
        return distinctKeys.size();
    }

    public SourceLookupKey get(int i) {
        return distinctKeys.get(i);
    }
}
