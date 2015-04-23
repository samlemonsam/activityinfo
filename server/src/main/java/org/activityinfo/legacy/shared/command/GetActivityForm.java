package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;

/**
 * Retrieves a FormClass (ActivityDTO for now)
 *
 */
public class GetActivityForm implements Command<ActivityFormDTO> {

    private int activityId;

    public GetActivityForm() {
    }

    public GetActivityForm(int activityId) {
        this.activityId = activityId;
    }

    public GetActivityForm(ActivityDTO activity) {
        this(activity.getId());
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }
}
