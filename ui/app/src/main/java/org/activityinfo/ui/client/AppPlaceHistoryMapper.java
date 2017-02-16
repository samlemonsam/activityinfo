package org.activityinfo.ui.client;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.AnalysisPlace;
import org.activityinfo.ui.client.input.RecordPlace;
import org.activityinfo.ui.client.table.TablePlace;

public class AppPlaceHistoryMapper implements PlaceHistoryMapper {

    @Override
    public Place getPlace(String token) {
        String[] parts = token.split("/");
        if(parts[0].equals("table")) {
            return new TablePlace(ResourceId.valueOf(parts[1]));
        } else if(parts[0].equals("analysis")) {
            return new AnalysisPlace();

        } else if(parts[0].equals("record")) {
            ResourceId formId = ResourceId.valueOf(parts[1]);
            ResourceId recordId = ResourceId.valueOf(parts[2]);
            return new RecordPlace(formId, recordId);

        } else {
            return null;
        }
    }

    @Override
    public String getToken(Place place) {
        if(place instanceof TablePlace) {
            return "table/" + ((TablePlace) place).getFormId().asString();
        } else if(place instanceof AnalysisPlace) {
            return "analysis";
        } else if(place instanceof RecordPlace) {
            return "record/" + ((RecordPlace) place).getFormId() + "/" + ((RecordPlace) place).getRecordId();
        }
        return null;
    }
}
