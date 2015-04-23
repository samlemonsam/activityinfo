package org.activityinfo.legacy.shared.adapter.bindings;

import com.google.common.base.Preconditions;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import static org.activityinfo.model.legacy.CuidAdapter.getLegacyIdFromCuid;
import static org.activityinfo.model.legacy.CuidAdapter.locationField;

/**
 * Defines a two-way binding between Sites and FormInstances
 */
public class SiteBinding extends ModelBinding<SiteDTO> {

    private final ActivityFormDTO activity;

    protected SiteBinding(ActivityFormDTO activity) {
        super(CuidAdapter.activityFormClass(activity.getId()), CuidAdapter.SITE_DOMAIN);
        this.activity = activity;
    }

    public ActivityFormDTO getActivity() {
        return activity;
    }

    public ResourceId getLocationField() {
        return locationField(activity.getId());
    }

    public int getAdminEntityId(FormInstance instance) {
        return getLegacyIdFromCuid(instance.getInstanceId(getLocationField()));
    }

    public LocationTypeDTO getLocationType() {
        return activity.getLocationType();
    }

    public int getDefaultPartnerId() {
        if(activity.getCurrentPartnerId() == 0) {
            // for database owners, return the first partner arbitrarily
            Preconditions.checkState(activity.isEditAllAllowed(), "user has no partnerId and does not have editAll permission");
            Preconditions.checkState(!activity.getPartnerRange().isEmpty());
            return activity.getPartnerRange().get(0).getId();

        } else {
            return activity.getCurrentPartnerId();
        }
    }
}
