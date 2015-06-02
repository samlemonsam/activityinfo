package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.geoadmin.merge2.view.match.FieldMatching;
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

    public SourceKeySet(FieldMatching fieldMatching) {

        // Which columns do we use to lookup the reference field? 
        // For example: if we are looking up the id of a territory, then 
        // we might use the territory name, territory code, and province name 
        // fields present in a shapefile or other imported set

        for (FieldProfile targetField : fieldMatching.getTarget().getFields()) {
            Optional<FieldProfile> sourceField = fieldMatching.targetToSource(targetField);
            if(sourceField.isPresent()) {
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
