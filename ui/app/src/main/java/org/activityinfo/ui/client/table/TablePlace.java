package org.activityinfo.ui.client.table;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import org.activityinfo.model.resource.ResourceId;

/**
 * Created by alex on 27-1-17.
 */
public class TablePlace extends Place {

    private ResourceId formId;

    public TablePlace(ResourceId formId) {
        this.formId = formId;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public static class Tokenizer implements PlaceTokenizer<TablePlace> {
        @Override
        public TablePlace getPlace(String token) {
            return new TablePlace(ResourceId.valueOf(token));
        }

        @Override
        public String getToken(TablePlace place) {
            return place.getFormId().asString();
        }
    }
}
