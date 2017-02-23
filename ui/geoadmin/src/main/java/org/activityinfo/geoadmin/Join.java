package org.activityinfo.geoadmin;

import org.activityinfo.geoadmin.model.AdminEntity;

/**
 * Represents a join between an existing AdminEntity and an imported feature.
 * 
 */
public class Join {
    private AdminEntity entity;
    private ImportFeature feature;

    public Join(AdminEntity adminUnit, ImportFeature bestFeature) {
        this.entity = adminUnit;
        this.feature = bestFeature;
    }

    public AdminEntity getEntity() {
        return entity;
    }

    public ImportFeature getFeature() {
        return feature;
    }
}
