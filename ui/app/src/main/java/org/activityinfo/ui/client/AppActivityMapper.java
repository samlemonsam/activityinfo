package org.activityinfo.ui.client;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import org.activityinfo.ui.client.analysis.AnalysisActivity;
import org.activityinfo.ui.client.analysis.AnalysisPlace;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.TableActivity;
import org.activityinfo.ui.client.table.TablePlace;


public class AppActivityMapper implements ActivityMapper {

    private FormStore formStore;

    public AppActivityMapper(FormStore formStore) {
        this.formStore = formStore;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof TablePlace) {
            return new TableActivity(formStore, (TablePlace) place);
        }

        if (place instanceof AnalysisPlace) {
            return new AnalysisActivity(formStore);
        }
        return null;
    }
}
