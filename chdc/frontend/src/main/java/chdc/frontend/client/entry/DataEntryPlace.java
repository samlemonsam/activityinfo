package chdc.frontend.client.entry;

import com.google.gwt.place.shared.Place;
import org.activityinfo.model.resource.ResourceId;

public class DataEntryPlace extends Place {

    public static final java.lang.String SLUG = "entry";

    private String formId;
    private String recordId;

    public DataEntryPlace(String formId, String recordId) {
        this.formId = formId;
        this.recordId = recordId;
    }

    public DataEntryPlace(String formId) {
        this.formId = formId;
        this.recordId = ResourceId.generateCuid();
    }

    @Override
    public String toString() {
        return "entry/" + formId + "/" + recordId;
    }
}
