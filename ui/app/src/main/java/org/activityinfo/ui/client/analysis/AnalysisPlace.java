package org.activityinfo.ui.client.analysis;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class AnalysisPlace extends Place {

    private String id;

    public AnalysisPlace(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
