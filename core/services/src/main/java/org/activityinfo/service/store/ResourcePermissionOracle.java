package org.activityinfo.service.store;

/**
 * Provides permission information for a specific user and ResourceCollection 
 */
public interface ResourcePermissionOracle {

    /**
     * 
     * @return true if the collection is it all visible to the user 
     */
    boolean isVisible();
    
    
}
