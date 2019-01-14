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
package org.activityinfo.model.permission;

import com.google.common.base.Strings;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.database.RecordLockSet;

import java.util.Optional;

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
public final class FormPermissions implements JsonSerializable {

    private boolean view;
    private String viewFilter;

    private boolean createRecord;
    private String createFilter;

    private boolean editRecord;
    private String editFilter;

    private boolean deleteRecord;
    private String deleteFilter;

    private boolean updateSchema;

    private boolean exportRecords;

    private RecordLockSet locks;

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

    public boolean isCreateAllowed() {
        return createRecord;
    }

    public String getCreateFilter() {
        return createFilter;
    }

    public boolean isEditAllowed() {
        return editRecord;
    }

    public String getEditFilter() {
        return editFilter;
    }

    public boolean isDeleteAllowed() {
        return deleteRecord;
    }

    public String getDeleteFilter() {
        return deleteFilter;
    }

    public boolean isSchemaUpdateAllowed() {
        return updateSchema;
    }

    public boolean isExportRecordsAllowed() {
        return exportRecords;
    }

    public RecordLockSet getLocks() {
        if (locks == null) {
            return RecordLockSet.EMPTY;
        }
        return locks;
    }

    public boolean hasLocks() {
        return locks != null;
    }

    public static FormPermissions none() {
        return new FormPermissions();
    }

    public static FormPermissions readonly() {
        FormPermissions permissions = new FormPermissions();
        permissions.view = true;
        permissions.exportRecords = true;
        return permissions;
    }

    public static FormPermissions readWrite() {
        FormPermissions permissions = new FormPermissions();
        permissions.view = true;
        permissions.createRecord = true;
        permissions.editRecord = true;
        permissions.deleteRecord = true;
        permissions.exportRecords = true;
        return permissions;
    }

    public static FormPermissions owner() {
        FormPermissions permissions = new FormPermissions();
        permissions.view = true;
        permissions.createRecord = true;
        permissions.editRecord = true;
        permissions.deleteRecord = true;
        permissions.updateSchema = true;
        permissions.exportRecords = true;
        return permissions;
    }

    public static FormPermissions fromJson(JsonValue object) {
        FormPermissions permissions = new FormPermissions();

        permissions.view = object.getBoolean("view");
        permissions.viewFilter = object.getString("viewFilter");

        permissions.createRecord = object.getBoolean("createRecord");
        permissions.createFilter = object.getString("createFilter");

        permissions.editRecord = object.getBoolean("editRecord");
        permissions.editFilter = object.getString("editFilter");

        permissions.deleteRecord = object.getBoolean("deleteRecord");
        permissions.deleteFilter = object.getString("deleteFilter");

        permissions.exportRecords = object.getBoolean("exportRecords");

        if (object.hasKey("locks") && object.get("locks").isJsonArray()) {
            permissions.locks = RecordLockSet.fromJson(object.get("locks"));
        }

        return permissions;
    }

    public JsonValue toJson() {
        JsonValue object = Json.createObject();

        object.put("view", view);
        object.put("viewFilter", viewFilter);

        object.put("createRecord", createRecord);
        object.put("createFilter", createFilter);

        object.put("editRecord", editRecord);
        object.put("editFilter", editFilter);

        object.put("deleteRecord", deleteRecord);
        object.put("deleteFilter", deleteFilter);

        object.put("exportRecords", exportRecords);

        if (hasLocks()) {
            object.put("locks", locks.toJson());
        }

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
                ", createAllowed=" + createRecord +
                ", createFilter='" + createFilter + '\'' +
                ", editAllowed=" + editRecord +
                ", editFilter='" + editFilter + '\'' +
                ", deleteAllowed=" + deleteRecord +
                ", deleteFilter='" + deleteFilter + '\'' +
                ", exportAllowed='" + exportRecords + '\'' +
                ", locks='" + locks +
                '}';
    }

    public boolean hasVisibilityFilter() {
        return !Strings.isNullOrEmpty(viewFilter);
    }

    public boolean hasCreateFilter() {
        return !Strings.isNullOrEmpty(createFilter);
    }

    public boolean hasEditFilter() {
        return !Strings.isNullOrEmpty(editFilter);
    }

    public boolean hasDeleteFilter() {
        return !Strings.isNullOrEmpty(deleteFilter);
    }

    public boolean isAllowed(Operation operation) {
        switch (operation) {
            case VIEW:
                return isVisible();
            case CREATE_RECORD:
                return createRecord;
            case EDIT_RECORD:
                return editRecord;
            case DELETE_RECORD:
                return deleteRecord;
            case EXPORT_RECORDS:
                return exportRecords;
        }
        throw new IllegalArgumentException("operation: " + operation);
    }

    /**
     * Returns true if permission to execute the given permission depends on
     * the record's field values.
     */
    public boolean isFiltered(Operation operation) {
        switch (operation) {
            case VIEW:
            case EXPORT_RECORDS:
                return hasVisibilityFilter();
            case CREATE_RECORD:
                return hasCreateFilter();
            case EDIT_RECORD:
                return hasEditFilter();
            case DELETE_RECORD:
                return hasDeleteFilter();
        }
        return false;
    }

    public String getFilter(Operation operation) {
       switch (operation) {
           case VIEW:
           case EXPORT_RECORDS:
               return viewFilter;
           case CREATE_RECORD:
               return createFilter;
           case EDIT_RECORD:
               return editFilter;
           case DELETE_RECORD:
               return deleteFilter;
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

        public Builder forbidView() {
            permissions.view = false;
            return this;
        }

        public boolean isAllowedView() {
            return permissions.view;
        }

        public Builder allowSchemaUpdate() {
            permissions.updateSchema = true;
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

        public Builder allowCreate(Optional<String> filter) {
            permissions.createRecord = true;
            permissions.createFilter = filter.orElse(null);
            return this;
        }

        public Builder allowEdit(Optional<String> filter) {
            permissions.editRecord = true;
            permissions.editFilter = filter.orElse(null);
            return this;
        }

        public Builder allowDelete(Optional<String> filter) {
            permissions.deleteRecord = true;
            permissions.deleteFilter = filter.orElse(null);
            return this;
        }

        public Builder allowExport() {
            permissions.exportRecords = true;
            return this;
        }

        public Builder lock(RecordLockSet locks) {
            permissions.locks = locks;
            return this;
        }

        public FormPermissions build() {
            return permissions;
        }

    }
}
