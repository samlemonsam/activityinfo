package org.activityinfo.ui.client.input;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

public class RecordPlace extends Place {

    private ResourceId formId;
    private ResourceId recordId;

    public RecordPlace(ResourceId formId, ResourceId recordId) {
        this.formId = formId;
        this.recordId = recordId;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public ResourceId getRecordId() {
        return recordId;
    }

    public RecordRef getRecordRef() {
        return new RecordRef(formId, recordId);
    }

    public static class Tokenizer implements PlaceTokenizer<RecordPlace> {
        @Override
        public RecordPlace getPlace(String token) {
            String[] parts = token.split("/");
            ResourceId formId = ResourceId.valueOf(parts[0]);
            ResourceId recordId = ResourceId.valueOf(parts[1]);

            return new RecordPlace(formId, recordId);
        }

        @Override
        public String getToken(RecordPlace place) {
            return place.getFormId() + "/" + place.getRecordId();
        }
    }
}
