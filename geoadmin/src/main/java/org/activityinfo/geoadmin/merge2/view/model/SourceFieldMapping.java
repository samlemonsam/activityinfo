package org.activityinfo.geoadmin.merge2.view.model;

import com.google.common.base.Optional;

import java.awt.*;

/**
 * Describes how a <em>source</em> field is mapped to the <em>target</em> form
 */
public class SourceFieldMapping {

    private FieldProfile sourceField;
    private Optional<FieldProfile> targetField;

    public SourceFieldMapping(FieldProfile sourceField, Optional<FieldProfile> targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }

    public FieldProfile getSourceField() {
        return sourceField;
    }
    
    public Optional<FieldProfile> getTargetField() {
        return targetField;
    }

    public String getLabel() {
        return sourceField.getLabel();
    }

    @Override
    public String toString() {
        if(targetField.isPresent()) {
            return sourceField.getLabel() + " → " + targetField.get().getLabel(); 
        } else {
            return sourceField.getLabel();
        }
    }
}
