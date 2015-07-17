package org.activityinfo.server.database.hibernate.entity;

import org.activityinfo.model.resource.ResourceId;

/**
 * Common interface to Attribute and Indicator entities
 */
public interface FormFieldEntity {

    int getId();

    ResourceId getFieldId();

    String getName();

    int getSortOrder();

    void delete();
}
