package org.activityinfo.ui.client.page.entry.form;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;

/**
 * LocationFormSection for sites with no location
 */
public class NullLocationFormSection implements LocationFormSection {

    private final LocationTypeDTO locationType;

    public NullLocationFormSection(LocationTypeDTO locationType) {
        this.locationType = locationType;
    }

    @Override
    public void updateForm(LocationDTO location, boolean isNew) {

    }

    @Override
    public void save(AsyncCallback<Void> callback) {
        callback.onSuccess(null);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void updateModel(SiteDTO m) {
        // as hack to support "No" location, we create a "None"
        // location type for each country and create a Location object
        // with the same id and the name of the country
        m.setLocationId(locationType.getId());
    }


    @Override
    public void updateForm(SiteDTO m, boolean isNew) {

    }

    @Override
    public Component asComponent() {
        throw new UnsupportedOperationException();
    }
}
