package org.activityinfo.ui.client;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import org.activityinfo.ui.client.data.FormService;
import org.activityinfo.ui.client.table.TableActivity;
import org.activityinfo.ui.client.table.TablePlace;


public class AppActivityMapper implements ActivityMapper {

    private FormService formService;

    public AppActivityMapper(FormService formService) {
        this.formService = formService;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof TablePlace) {
            return new TableActivity(formService, (TablePlace) place);
        }

        return null;
    }
}
