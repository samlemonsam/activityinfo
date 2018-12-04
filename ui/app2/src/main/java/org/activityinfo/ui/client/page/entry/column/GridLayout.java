package org.activityinfo.ui.client.page.entry.column;


import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.model.resource.ResourceId;

public class GridLayout {
    private final ColumnModel columnModel;
    private final String heading;
    private final boolean suspended;
    private final boolean owner;
    private final ResourceId formId;
    private final ActivityFormDTO activity;

    private GridLayout(ColumnModel columnModel, String heading,
                       boolean suspended,
                       boolean owner,
                       ResourceId formId,
                       ActivityFormDTO activity) {
        this.columnModel = columnModel;
        this.heading = heading;
        this.suspended = suspended;
        this.owner = owner;
        this.formId = formId;
        this.activity = activity;
    }

    public static GridLayout classic(String heading, ActivityFormDTO activity, ColumnModel columnModel) {
        return new GridLayout(columnModel, heading, false, false, null, activity);
    }

    public static GridLayout classic(String heading, ColumnModel columnModel) {
        return new GridLayout(columnModel, heading, false, false, null, null);
    }

    public static GridLayout suspended(String heading, boolean owner) {
        return new GridLayout(null, heading, true, owner, null, null);
    }

    public static GridLayout redirect(String heading, ResourceId formId) {
        return new GridLayout(null, heading, false, false, formId, null);
    }

    public ColumnModel getColumnModel() {
        return columnModel;
    }

    public String getHeading() {
        return heading;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isVisibleClassic() {
        return !suspended && formId == null;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public boolean isSingleClassicActivity() {
        return activity != null;
    }

    public ActivityFormDTO getActivity() {
        return activity;
    }
}
