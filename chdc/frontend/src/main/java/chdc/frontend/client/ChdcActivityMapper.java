package chdc.frontend.client;

import chdc.frontend.client.entry.DataEntryActivity;
import chdc.frontend.client.entry.DataEntryPlace;
import chdc.frontend.client.table.TableActivity;
import chdc.frontend.client.table.TablePlace;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;


public class ChdcActivityMapper implements ActivityMapper {

    @Override
    public Activity getActivity(Place place) {
        if(place instanceof DataEntryPlace) {
            return new DataEntryActivity();
        } else if(place instanceof TablePlace) {
            return new TableActivity();
        }
        return null;
    }
}
