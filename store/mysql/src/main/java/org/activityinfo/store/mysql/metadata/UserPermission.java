package org.activityinfo.store.mysql.metadata;


public class UserPermission {
    boolean view;
    boolean viewAll;
    int partnerId;

    public boolean isView() {
        return view;
    }

    public boolean isViewAll() {
        return viewAll;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public static UserPermission viewAll() {
        UserPermission permission = new UserPermission();
        permission.viewAll = true;
        permission.view = true;
        return permission;
    }
    
}
