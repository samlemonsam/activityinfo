package chdc.frontend.client.entry;

import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

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

    public DataEntryPlace(ResourceId formId) {
        this(formId.asString());
    }

    public RecordRef getRecordRef() {
        return new RecordRef(ResourceId.valueOf(formId), ResourceId.valueOf(recordId));
    }

    public SafeUri toUri() {
        return UriUtils.fromTrustedString("#" + toString());
    }

    @Override
    public String toString() {
        return "entry/" + formId + "/" + recordId;
    }
}
