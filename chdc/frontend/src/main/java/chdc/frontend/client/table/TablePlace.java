package chdc.frontend.client.table;

import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.model.resource.ResourceId;

public class TablePlace extends Place {

    public static final String SLUG = "table";

    private final String formId;

    public TablePlace(String formId) {
        this.formId = formId;
    }

    public TablePlace(ResourceId formId) {
        this(formId.asString());
    }

    public SafeUri toUri() {
        return UriUtils.fromTrustedString("#" + toString());
    }

    @Override
    public String toString() {
        return "table/" + formId;
    }
}
