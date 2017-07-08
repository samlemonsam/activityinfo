package org.activityinfo.model.form;

import com.google.common.base.Strings;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;

/**
 * Describes effective form-level permissions for a specific user. The principle permissions include:
 *
 * <ul>
 *     <li>create: the permission to create a new record.</li>
 *     <li>view: the permission to view this form</li>
 *     <li>edit: the permission to edit the field values of records belonging to this form</li>
 *     <li>delete: the permission to delete records belonging to the form</li>
 * </ul>
 *
 * In addition, the view, edit, and delete permissions may be further restricted by specifying
 * view, edit, and delete <em>filters</em>, using formulas that evaluate to a boolean value.</p>
 */
public final class FormPermissions {

    private boolean view;

    /**
     * True if the user has permission to create new records
     */
    private boolean createRecord;

    private boolean updateRecord;

    private boolean deleteRecord;

    private String viewFilter;

    private String updateFilter;

    /**
     * 
     * @return true if this form is visible to the user.
     */
    public boolean isVisible() {
        return view;
    }

    public String getViewFilter() {
        return viewFilter;
    }

    public FormPermissions setViewFilter(String viewFilter) {
        this.viewFilter = viewFilter;
        return this;
    }

    public boolean isEditAllowed() {
        return updateRecord;
    }

    
    public boolean isCreateAllowed() {
        return createRecord;
    }

    public void setCreateAllowed(boolean createAllowed) {
        this.createRecord = createAllowed;
    }

    public boolean isDeleteAllowed() {
        return deleteRecord;
    }

    public void setDeleteAllowed(boolean deleteAllowed) {
        this.deleteRecord = deleteAllowed;
    }

    public FormPermissions setEditAllowed(boolean editAllowed) {
        this.updateRecord = editAllowed;
        return this;
    }

    
    public String getUpdateFilter() {
        return updateFilter;
    }

    public void setUpdateFilter(String updateFilter) {
        this.updateFilter = updateFilter;
    }
    public static FormPermissions none() {
        return new FormPermissions();
    }

    public static FormPermissions readonly() {
        FormPermissions permissions = new FormPermissions();
        permissions.view = true;
        return permissions;
    }

    public static FormPermissions full() {
        FormPermissions permissions = new FormPermissions();
        permissions.view = true;
        permissions.createRecord = true;
        permissions.updateRecord = true;
        permissions.deleteRecord = true;
        return permissions;
    }

    public static FormPermissions fromJson(JsonObject object) {
        FormPermissions permissions = new FormPermissions();
        permissions.view = object.getBoolean("view");
        permissions.createRecord = object.getBoolean("createRecord");
        permissions.updateRecord = object.getBoolean("updateRecord");
        permissions.deleteRecord = object.getBoolean("deleteRecord");
        permissions.viewFilter = object.getString("viewFilter");
        permissions.updateFilter = object.getString("updateFilter");
        return permissions;
    }

    public JsonObject toJson() {
        JsonObject object = Json.createObject();
        object.put("view", view);
        object.put("createRecord", createRecord);
        object.put("updateRecord", updateRecord);
        object.put("deleteRecord", deleteRecord);
        object.put("viewFilter", viewFilter);
        object.put("updateFilter", updateFilter);
        return object;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "CollectionPermissions{" +
                "visible=" + view +
                ", visibilityFilter='" + viewFilter + '\'' +
                ", editAllowed=" + updateRecord +
                ", editFilter='" + updateFilter + '\'' +
                '}';
    }

    public boolean hasVisibilityFilter() {
        return !Strings.isNullOrEmpty(viewFilter);
    }

    public boolean isAllowed(FormOperation operation) {
        switch (operation) {
            case VIEW:
                return isVisible();
            case CREATE_RECORD:
                return createRecord;
            case EDIT_RECORD:
                return updateRecord;
            case DELETE_RECORD:
                return deleteRecord;
        }
        throw new IllegalArgumentException("operation: " + operation);
    }

    /**
     * Returns true if permission to execute the given permission depends on
     * the record's field values.
     */
    public boolean isFiltered(FormOperation operation) {
        switch (operation) {
            case VIEW:
                return !Strings.isNullOrEmpty(viewFilter);
            case CREATE_RECORD:
            case DELETE_RECORD:
            case EDIT_RECORD:
                return !Strings.isNullOrEmpty(updateFilter);
        }
        return false;
    }

    public String getFilter(FormOperation operation) {
       switch (operation) {
           case VIEW:
               return viewFilter;
           case CREATE_RECORD:
           case DELETE_RECORD:
           case EDIT_RECORD:
               return updateFilter;
       }
       return null;
    }

    public static class Builder {
        private FormPermissions permissions = new FormPermissions();

        /**
         * Allows viewing this form, its schema, and all of its records,
         */
        public Builder allowView() {
            permissions.view = true;
            return this;
        }

        /**
         * Allows viewing this form, its schema, and records that match the given formula.
         *
         * @param filter a boolean-valued formula
         */
        public Builder allowFilteredView(String filter) {
            permissions.view = true;
            permissions.viewFilter = filter;
            return this;
        }


        public void allowUnfilteredView() {
            permissions.view = true;
            permissions.viewFilter = null;
        }

        /**
         * Allows creating, editing, and deleting this form's records. It also implies that the form
         * is visible.
         */
        public Builder allowEdit() {
            allowView();
            permissions.updateRecord = true;
            permissions.createRecord = true;
            permissions.deleteRecord = true;
            return this;
        }

        /**
         * Allows creating, editing, and deleting the form's records which match the given filter.
         */
        public Builder allowFilteredEdit(String filter) {
            allowEdit();
            permissions.updateFilter = filter;
            return this;
        }

        public FormPermissions build() {
            return permissions;
        }

    }
}
