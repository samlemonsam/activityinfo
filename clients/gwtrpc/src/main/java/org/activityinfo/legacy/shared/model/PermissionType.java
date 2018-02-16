package org.activityinfo.legacy.shared.model;

public enum PermissionType {

    VIEW                ("allowView"),
    EDIT                ("allowEdit"),
    MANAGE_USERS        ("allowManageUsers"),
    VIEW_ALL            ("allowViewAll"),
    EDIT_ALL            ("allowEditAll"),
    MANAGE_ALL_USERS    ("allowManageAllUsers"),
    DESIGN              ("allowDesign");

    private final String dtoPropertyName;

    PermissionType(String dtoPropertyName) {
        this.dtoPropertyName = dtoPropertyName;
    }

    public String getDtoPropertyName() {
        return dtoPropertyName;
    }

}
