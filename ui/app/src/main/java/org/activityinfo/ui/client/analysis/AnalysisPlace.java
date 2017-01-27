package org.activityinfo.ui.client.analysis;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class AnalysisPlace extends Place {

    public static class Tokenizer implements PlaceTokenizer<AnalysisPlace> {
        @Override
        public AnalysisPlace getPlace(String token) {
            return new AnalysisPlace();
        }

        @Override
        public String getToken(AnalysisPlace place) {
            return "";
        }
    }
}
