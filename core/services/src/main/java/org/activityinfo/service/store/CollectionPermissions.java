package org.activityinfo.service.store;

import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.expr.ExprValue;

/**
 * Describes collection-level permissions for a specific user
 */
public final class CollectionPermissions implements IsRecord {

    private boolean visible;
    private String visibilityFilter;
    private boolean editAllowed;
    private String editFilter;


    /**
     * 
     * @return true if this collection is visible to the user.
     */
    public boolean isVisible() {
        return visible;
    }

    public CollectionPermissions setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public String getVisibilityFilter() {
        return visibilityFilter;
    }

    public CollectionPermissions setVisibilityFilter(String visibilityFilter) {
        this.visibilityFilter = visibilityFilter;
        return this;
    }

    public boolean isEditAllowed() {
        return editAllowed;
    }

    
    public CollectionPermissions setEditAllowed(boolean editAllowed) {
        this.editAllowed = editAllowed;
        return this;
    }

    public String getEditFilter() {
        return editFilter;
    }

    public void setEditFilter(String editFilter) {
        this.editFilter = editFilter;
    }

    @Override
    public Record asRecord() {
        Record record = new Record()
                .set("visible", visible)
                .set("editAllowed", editAllowed);
        
        if(visibilityFilter != null) {
            record.set("visibilityFilter", new ExprValue(visibilityFilter).asRecord());
        }
        if(editFilter != null) {
            record.set("editFilter", new ExprValue(editFilter).asRecord());
        }
        return record;
    }
    
    public static CollectionPermissions none() {
        return new CollectionPermissions()
                .setVisible(false)
                .setEditAllowed(false);
    }
    
    public static CollectionPermissions readonly() {
        return new CollectionPermissions()
                .setVisible(true)
                .setEditAllowed(false);
    }
    
    public static CollectionPermissions full() {
        return new CollectionPermissions()
                .setVisible(true)
                .setEditAllowed(true);
    }
}
