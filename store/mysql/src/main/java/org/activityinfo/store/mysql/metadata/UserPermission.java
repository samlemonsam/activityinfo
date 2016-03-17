package org.activityinfo.store.mysql.metadata;


import java.io.Serializable;

public class UserPermission implements Serializable {
    boolean view;
    boolean viewAll;
    boolean edit;
    boolean editAll;
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

    public boolean isEdit() {
        return edit;
    }

    public boolean isEditAll() {
        return editAll;
    }

    public static UserPermission viewAll() {
        UserPermission permission = new UserPermission();
        permission.viewAll = true;
        permission.view = true;
        return permission;
    }
    
}
