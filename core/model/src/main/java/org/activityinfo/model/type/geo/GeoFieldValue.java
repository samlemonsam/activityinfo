package org.activityinfo.model.type.geo;

import org.activityinfo.model.type.FieldValue;

/**
 * Common interface to FieldValues of geographic type
 */
public interface GeoFieldValue extends FieldValue {
    
    Extents getEnvelope();
}
