package org.activityinfo.ui.client;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

public class App3 {
    public static void openNewTable(Integer activityId) {
        openNewTable( CuidAdapter.activityFormClass(activityId));
    }

    public static void openNewTable(ResourceId formId) {
        com.google.gwt.user.client.Window.open("/app?ui=3#table/" + formId.asString(), "_blank", null);
    }
}
